package pl.bzowski.surveys;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.question.Question;
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

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Question> questions;

    public Survey() {}

    public Survey(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    // Przykładowa metoda tworząca DTO z Survey do REST API
    public SurveyDTO toDTO() {
        return new SurveyDTO(this.id, this.name, this.questions.stream().map(Question::toDTO).toList());
    }
}
