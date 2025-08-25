package pl.bzowski.question;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.surveys.Survey;

import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String title;

    @Column(length = 2048)
    public String description;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    public Survey survey;

    public Question() {}

    public Question(String title, String description, Survey survey) {
        this.title = title;
        this.description = description;
        this.survey = survey;
    }

    public QuestionDTO toDTO() {
        return new QuestionDTO(this.id, this.title, this.description);
    }
}