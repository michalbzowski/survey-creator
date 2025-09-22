package pl.bzowski.attendance_list.infrastructure;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.POST;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.attendance_list.api.AttendanceListDTO;

import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
public class AttendanceListRepository extends RepositoryBase {

    @POST
    @WithTransaction
    public Uni<AttendanceListDTO> createAttendanceList(AttendanceListDTO attendanceListDTO) {
        List<Uni<Event>> eventUnis = attendanceListDTO.events.stream()
                .map(id -> Event.<Event>findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + id))
                        .onItem().invoke(event -> {
                            if (event.attendanceList != null) {
                                throw new IllegalArgumentException("Wydarzenie jest już przypisane do listy obecności");
                            }
                        }))
                .collect(Collectors.toList());

        return Uni.combine().all().unis(eventUnis)
                .with(list -> (List<Event>) list)
                .flatMap(events -> {
                    return currentRegisteredUserId().flatMap(uuid -> {
                        AttendanceList attendanceList = new AttendanceList();
                        attendanceList.name = attendanceListDTO.name;
                        attendanceList.events = events;
                        attendanceList.registeredUserId = uuid;
                        return Panache.withTransaction(attendanceList::persist)
                                .onItem().transform(v -> {
                                    attendanceListDTO.id = attendanceList.id;
                                    for (Event ev : events) {
                                        ev.attendanceList = attendanceList;
                                    }
                                    return attendanceList;
                                });
                    });

                })
                .onFailure().recoverWithNull()
                .onItem().transform(AttendanceList::toDTO);
    }

    public Uni<List<AttendanceList>> listAll() {
        return AttendanceList.list("registeredUserId", currentRegisteredUserId());
    }
}
