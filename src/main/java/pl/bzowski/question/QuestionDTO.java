package pl.bzowski.question;

import java.util.UUID;

public class QuestionDTO {
    public UUID id;
    public String title;
    public String description;

    // Ważne: konstruktor bezparametrowy wymagany przez Jackson
    public QuestionDTO() {
    }

    public QuestionDTO(UUID id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
}