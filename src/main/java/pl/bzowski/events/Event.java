package pl.bzowski.events;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    public String description;

    public Event() {
    }

    public Event(String name, String location, LocalDateTime localDateTime, String description) {
        this.name = name;
        this.location = location;
        this.localDateTime = localDateTime;
        this.description = description;
    }
}
