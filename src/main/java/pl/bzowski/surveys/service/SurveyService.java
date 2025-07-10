package pl.bzowski.surveys.service;

import jakarta.enterprise.context.ApplicationScoped;
import pl.bzowski.surveys.Survey;

@ApplicationScoped
public class SurveyService {
    public Survey createSurvey(Survey survey) {
        if (survey == null || survey.title == null || survey.description == null) {
            throw new IllegalArgumentException("Brakuje wymaganych danych");
        }
        survey.persist();
        return survey;
    }
}
