package pl.bzowski.attendances.list.infrastructure;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.hibernate.reactive.mutiny.Mutiny;
import pl.bzowski.attendances.list.AttendanceList;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.attendances.list.api.AttendanceListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class AttendanceListRepository extends RepositoryBase {

    @WithTransaction
    public Uni<AttendanceListDTO> createAttendanceList(AttendanceListDTO attendanceListDTO) {
        List<UUID> eventIds = attendanceListDTO.events;

        // Start with an initial Uni emitting an empty list of Event
        Uni<List<Event>> eventsUni = Uni.createFrom().item(new ArrayList<>());

        // Sequentially process event IDs one by one, accumulating validated Event instances
        for (UUID eventId : eventIds) {
            eventsUni = eventsUni.flatMap(events ->
                    Event.<Event>findById(eventId)
                            .onItem().ifNull().failWith(() -> new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + eventId))
                            .onItem().invoke(event -> {
                                if (event.attendanceList != null) {
                                    throw new IllegalArgumentException("Wydarzenie jest już przypisane do listy obecności");
                                }
                            })
                            .map(event -> {
                                events.add(event);
                                return events;
                            })
            );
        }

        return eventsUni
                .flatMap(events ->
                        currentRegisteredUserId().flatMap(uuid -> {
                            AttendanceList attendanceList = new AttendanceList();
                            attendanceList.name = attendanceListDTO.name;
                            attendanceList.events = events;
                            attendanceList.registeredUserId = uuid;
                            return Panache.withTransaction(attendanceList::persist)
                                    .onItem().transform(_ -> {
                                        attendanceListDTO.id = attendanceList.id;
                                        for (Event ev : events) {
                                            ev.attendanceList = attendanceList;
                                        }
                                        return attendanceList;
                                    });
                        })
                )
                .onFailure().recoverWithNull()
                .onItem().transform(AttendanceList::toDTO);
    }


    public Uni<List<AttendanceList>> listAll() {
        return currentRegisteredUserId().flatMap(uuid -> AttendanceList.list("registeredUserId", uuid));
    }

    public Uni<List<AttendanceList>> listAllWithEvents() {
        return currentRegisteredUserId().flatMap(userId ->
                AttendanceList.<AttendanceList>list("registeredUserId", userId)
                        .flatMap(attendanceLists -> {
                            // Jawnie dla każdej listy wykonujemy fetch events
                            return Multi.createFrom().iterable(attendanceLists)
                                    .onItem().transformToUniAndConcatenate(attendanceList ->
                                            Mutiny.fetch(attendanceList.events)
                                                    .replaceWith(attendanceList)
                                    )
                                    .collect().asList();
                        })
        );
    }

}
