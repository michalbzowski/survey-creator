package pl.bzowski.links;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.surveys.Survey;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "person_survey_links")
public class PersonSurveyLink extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(optional = false)
    public Person person;

    @ManyToOne(optional = false)
    public Survey survey;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Enumerated(EnumType.STRING)
    public Answer answer;

    public enum Answer {
        YES, NO
    }

    public PersonSurveyLink() {
    }

    public PersonSurveyLink(Person person, Survey survey) {
        this.person = person;
        this.survey = survey;
        this.linkToken = UUID.randomUUID();
    }
}
