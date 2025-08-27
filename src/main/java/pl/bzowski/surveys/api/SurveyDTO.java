package pl.bzowski.surveys.api;

import pl.bzowski.events.web.EventDto;
import pl.bzowski.question.QuestionDTO;

import java.util.List;
import java.util.UUID;

public class SurveyDTO {
    public UUID id;
    public String name;
    public List<UUID> events;

    public SurveyDTO() {
    }

    public SurveyDTO(UUID id, String name, List<UUID> events) {
        this.id = id;
        this.name = name;
        this.events = events;
    }
}
