package pl.bzowski.attendance_list.infrastructure;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.attendance_list.api.AttendanceListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class AttendanceListRepository extends RepositoryBase {

    @POST
    @Transactional // lub lepiej @WithTransaction, jeśli masz tę adnotację w projekcie
    public Uni<Response> createAttendanceList(AttendanceListDTO attendanceListDTO) {
        List<Uni<Event>> eventUnis = attendanceListDTO.events.stream()
                .map(id -> Event.<Event>findById(id)
                        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + id))
                        .onItem().invoke(event -> {
                            if (event.attendanceList != null) {
                                throw new IllegalArgumentException("Wydarzenie jest już przypisane do listy obecności");
                            }
                        }))
                .collect(Collectors.toList());

        return Uni.combine().all().unis(eventUnis).with(list -> (List<Event>) list)
                .onItem().transformToUni(events -> {
                    AttendanceList attendanceList = new AttendanceList();
                    attendanceList.name = attendanceListDTO.name;
                    attendanceList.events = events;
                    attendanceList.registeredUserId = currentRegisteredUserId();
                    return Panache.withTransaction(attendanceList::persist)
                            .onItem().transform(v -> {
                                attendanceListDTO.id = attendanceList.id;
                                for (Event ev : events) {
                                    ev.attendanceList = attendanceList;
                                }
                                return Response.status(Response.Status.CREATED).entity(attendanceListDTO).build();
                            });
                })
                .onFailure().recoverWithItem(throwable ->
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(throwable.getMessage())
                                .build()
                );

    }

    public Uni<List<AttendanceList>> listAll() {
        return AttendanceList.list("registeredUserId", currentRegisteredUserId());
    }
}
