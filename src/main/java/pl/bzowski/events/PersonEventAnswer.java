package pl.bzowski.events;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.persons.Person;
import pl.bzowski.surveys.Survey;

import java.util.UUID;

@Entity
@Table(name = "person_event_answers")
public class PersonEventAnswer extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    public Person person;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    public Survey survey;

    @ManyToOne
    @JoinColumn(name = "event_id")
    public Event event;

    @Enumerated(EnumType.STRING)
    public Answer answer;

    public enum Answer {
        TAK, NIE, ODPOWIEM_POZNIEJ
    }
}
