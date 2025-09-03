package pl.bzowski.events;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.events.web.EventDto;
import pl.bzowski.security.RegisteredUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String location;

    @Column(nullable = false)
    public LocalDateTime localDateTime;

    @Column(nullable = false, length = 4096)
    public String description;

    @ManyToOne
    @JoinColumn(name = "attendance_list_id")
    public AttendanceList attendanceList;

    @ManyToOne
    @JoinColumn(name = "registered_user_id")
    public RegisteredUser registeredUser;

    public Event() {
    }

    public Event(String name, String location, LocalDateTime localDateTime, String description) {
        this.name = name;
        this.location = location;
        this.localDateTime = localDateTime;
        this.description = description;
    }

    public String formatedLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("pl", "PL"));
        return localDateTime.format(formatter);
    }

    public String nameWithFormatedLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("pl", "PL"));
        return localDateTime.format(formatter) + " " + name;
    }

    public EventDto toDTO() {
        return new EventDto(id, name, location, localDateTime, description);
    }
}
