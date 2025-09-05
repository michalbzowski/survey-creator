package pl.bzowski.attendance_list.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.attendance_list.api.AttendanceListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class AttendanceListRepository extends RepositoryBase {

    @Transactional
    public AttendanceListDTO createAttendanceList(AttendanceListDTO attendanceListDTO) {
//        if(attendanceListDTO.name == null || attendanceListDTO.name.isEmpty()) {
//            throw new IllegalArgumentException("Brakuje wymaganych danych");
//        }

        // Pobierz Eventy z bazy po ID przesłanych z DTO
        List<Event> events = new ArrayList<>();
        if (attendanceListDTO.events != null) {
            for (UUID id : attendanceListDTO.events) {
                Event event = Event.findById(id);
                if (event == null) {
                    throw new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + id);
                }
                if (event.attendanceList != null) {
                    throw new IllegalArgumentException("Wydarzenie jest już przypisane do listy obecnościy");
                }
                events.add(event);
            }
        }

        AttendanceList attendanceList = new AttendanceList();
        attendanceList.name = attendanceListDTO.name;
        attendanceList.events = events;
        attendanceList.registeredUserId = currentRegisteredUserId();
        attendanceList.persist();
        attendanceListDTO.id = attendanceList.id;
        // Opcjonalnie przypisz listę obecności do eventów (jeśli w Event masz relację odwrotną)
        for (Event ev : events) {
            ev.attendanceList = attendanceList;
        }

        return attendanceListDTO;
    }

    public List<AttendanceList> listAll() {
        return AttendanceList.list("registeredUserId", currentRegisteredUserId());
    }
}
