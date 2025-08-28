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

    @Column
    public UUID personId;

    @Column
    public String personFirstName;

    @Column
    public String personLastName;

    @Column
    public String personEmail;

    @Column
    public String personTag;

    @ManyToOne(optional = false)
    public Survey survey;

    @Column(nullable = false)
    public UUID surveyId;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Column(nullable = false)
    public Boolean surveyAnswered = false;

    public void sendingError() {
        this.status = SendingStatus.ERROR;
    }

    public enum SendingStatus {
        TO_SEND, SENT, ERROR
    }

    @Enumerated(EnumType.STRING)
    public SendingStatus status;

    public PersonSurveyLink() {
    }

    public PersonSurveyLink(Person person, Survey survey) {
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.survey = survey;
        this.surveyId = survey.id;
        this.linkToken = UUID.randomUUID();
        this.status = SendingStatus.TO_SEND;
    }

    public void sent() {
        status = SendingStatus.SENT;
    }
}
