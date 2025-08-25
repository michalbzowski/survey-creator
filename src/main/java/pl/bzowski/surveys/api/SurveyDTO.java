package pl.bzowski.surveys.api;

import pl.bzowski.question.QuestionDTO;

import java.util.List;
import java.util.UUID;

public class SurveyDTO {
    public UUID id;
    public String name;
    public List<QuestionDTO> questions;

    public SurveyDTO() {
    }

    public SurveyDTO(UUID id, String name, List<QuestionDTO> questions) {
        this.id = id;
        this.name = name;
        this.questions = questions;
    }
}
