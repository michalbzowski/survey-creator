package pl.bzowski.attendance_list;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.events.Event;
import pl.bzowski.attendance_list.api.AttendanceListDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "attendance_list")
public class AttendanceList extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column
    public String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "attendance_list_events",
            joinColumns = @JoinColumn(name = "attendance_list_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    public List<Event> events;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    public AttendanceList() {}

    public AttendanceList(String name, List<Event> events) {
        this.name = name;
        this.events = events;
    }

    // Przykładowa metoda tworząca DTO z AttendanceList do REST API
    public AttendanceListDTO toDTO() {
        return new AttendanceListDTO(this.id, this.name, this.events.stream().map(e -> e.id).toList());
    }

    public String joinedEventsName() {
        return events.stream().map(e -> e.name).collect(Collectors.joining(", "));
    }
}
