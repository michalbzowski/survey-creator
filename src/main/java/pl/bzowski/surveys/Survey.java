package pl.bzowski.surveys;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.events.Event;
import pl.bzowski.surveys.api.SurveyDTO;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "surveys")
public class Survey extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "survey_events",
            joinColumns = @JoinColumn(name = "survey_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    public List<Event> events;

    public Survey() {}

    public Survey(String name, List<Event> events) {
        this.name = name;
        this.events = events;
    }

    // Przykładowa metoda tworząca DTO z Survey do REST API
    public SurveyDTO toDTO() {
        return new SurveyDTO(this.id, this.name, this.events.stream().map(e -> e.id).toList());
    }
}
