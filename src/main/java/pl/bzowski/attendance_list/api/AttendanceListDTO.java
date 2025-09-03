package pl.bzowski.attendance_list.api;

import java.util.List;
import java.util.UUID;

public class AttendanceListDTO {
    public UUID id;
    public String name;
    public List<UUID> events;

    public AttendanceListDTO() {
    }

    public AttendanceListDTO(UUID id, String name, List<UUID> events) {
        this.id = id;
        this.name = name;
        this.events = events;
    }
}
