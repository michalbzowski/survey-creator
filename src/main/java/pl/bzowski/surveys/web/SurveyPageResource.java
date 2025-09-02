package pl.bzowski.surveys.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.events.Event;
import pl.bzowski.persons.Person;
import pl.bzowski.question.QuestionDTO;
import pl.bzowski.surveys.Survey;
import pl.bzowski.surveys.api.SurveyDTO;
import pl.bzowski.surveys.service.SurveyService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/web/surveys")
public class SurveyPageResource {



    private final Template createSurvey;
    private final Template listSurveys;
    private final SurveyService surveyService;
    private final JsonHelper jsonHelper;

    public SurveyPageResource(Template createSurvey, Template listSurveys, SurveyService surveyService, JsonHelper jsonHelper) {
        this.createSurvey = createSurvey;
        this.listSurveys = listSurveys;
        this.surveyService = surveyService;
        this.jsonHelper = jsonHelper;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createSurveyForm(@QueryParam("name") String name, @QueryParam("eventId") UUID eventId) {
        List<Event> availableEvents = Event.findAvailableEvents();
        List<Event> first = List.of(availableEvents.getFirst());
        String availableEventsJson = jsonHelper.toJson(availableEvents);
        return createSurvey.data("survey", new Survey("", first),
                "availableEvents", availableEvents,
                "availableEventsJson", availableEventsJson,
                "name", name,
                "eventId", eventId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createSurvey(SurveyDTO survey) {
        try {
            var dto = surveyService.createSurvey(survey);
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            // obsługa błędu, np. zwrócenie strony z komunikatem
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listQueries() {
        List<Survey> surveys = Survey.listAll();
        return listSurveys.data("surveys", surveys);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Survey survey = Survey.findById(id);
            if (survey != null) {
                survey.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/web/surveys").build()).build();
    }


}
