package pl.bzowski.events;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.events.web.EventDto;
import pl.bzowski.surveys.Survey;

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
    @JoinColumn(name = "survey_id")
    public Survey survey;

    public Event() {
    }

    public Event(String name, String location, LocalDateTime localDateTime, String description) {
        this.name = name;
        this.location = location;
        this.localDateTime = localDateTime;
        this.description = description;
    }

    public static List<Event> findAvailableEvents() {
        return list("survey is null");
    }

    public String formatedLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("pl", "PL"));
        return localDateTime.format(formatter);
    }

    public EventDto toDTO() {
        return new EventDto(id, name, location, localDateTime, description);
    }
}
