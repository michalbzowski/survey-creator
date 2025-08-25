package pl.bzowski.question;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.persons.Person;
import pl.bzowski.surveys.Survey;

import java.util.UUID;

@Entity
@Table(name = "person_question_answers")
public class PersonQuestionAnswer extends PanacheEntityBase {

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
    @JoinColumn(name = "question_id")
    public Question question;

    @Enumerated(EnumType.STRING)
    public Answer answer;

    public enum Answer {
        TAK, NIE, ODPOWIEM_POZNIEJ
    }
}
