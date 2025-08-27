package pl.bzowski.surveys.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import pl.bzowski.events.Event;
import pl.bzowski.events.web.EventDto;
import pl.bzowski.surveys.Survey;
import pl.bzowski.surveys.api.SurveyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SurveyService {

    @Transactional
    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        if(surveyDTO.name == null || surveyDTO.name.isEmpty()) {
            throw new IllegalArgumentException("Brakuje wymaganych danych");
        }

        // Pobierz Eventy z bazy po ID przesłanych z DTO
        List<Event> events = new ArrayList<>();
        if (surveyDTO.events != null) {
            for (UUID id : surveyDTO.events) {
                Event event = Event.findById(id);
                if (event == null) {
                    throw new IllegalArgumentException("Nie znaleziono wydarzenia o id: " + id);
                }
                if (event.survey != null) {
                    throw new IllegalArgumentException("Wydarzenie jest już przypisane do ankiety");
                }
                events.add(event);
            }
        }

        Survey survey = new Survey();
        survey.name = surveyDTO.name;
        survey.events = events;

        survey.persist();

        // Opcjonalnie przypisz ankietę do eventów (jeśli w Event masz relację odwrotną)
        for (Event ev : events) {
            ev.survey = survey;
        }

        return surveyDTO;
    }

}
