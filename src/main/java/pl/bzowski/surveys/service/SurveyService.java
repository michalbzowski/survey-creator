package pl.bzowski.surveys.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pl.bzowski.question.Question;
import pl.bzowski.surveys.Survey;
import pl.bzowski.surveys.api.SurveyDTO;

import java.util.List;

@ApplicationScoped
public class SurveyService {

    @Transactional
    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        if (surveyDTO == null || surveyDTO.name == null || surveyDTO.questions == null || surveyDTO.questions.isEmpty()) {
            throw new IllegalArgumentException("Brakuje wymaganych danych");
        }

        // Tworzymy nową encję Survey i ustawiamy nazwę
        Survey survey = new Survey();
        survey.name = surveyDTO.name;

        // Mapujemy pytania DTO na encje Question i przypisujemy do survey
        List<Question> questions = surveyDTO.questions.stream()
                .map(qDto -> {
                    Question question = new Question();
                    question.title = qDto.title;
                    question.description = qDto.description;
                    question.survey = survey;  // przypisujemy relację dwukierunkową
                    return question;
                }).toList();

        survey.questions = questions;

        // Persistujemy encję Survey wraz z pytaniami (kaskadowo)
        survey.persist();

        // Zwracamy utworzonego DTO na podstawie encji
        return survey.toDTO();
    }

}
