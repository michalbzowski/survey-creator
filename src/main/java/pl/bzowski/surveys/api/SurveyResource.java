package pl.bzowski.surveys.api;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.surveys.Survey;
import pl.bzowski.surveys.service.SurveyService;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/surveys")
public class SurveyResource {

    private final SurveyService surveyService;

    @Inject
    public SurveyResource(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GET
    public List<Survey> listAllSurveys() {
        return Survey.listAll();
    }

    @POST
    @Transactional
    public Response createSurvey(@BeanParam Survey survey) {
        try {
            Survey created = surveyService.createSurvey(survey);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteSurvey(@PathParam("id") UUID id) {
        Survey survey = Survey.findById(id);
        if (survey == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Query nie została znaleziona").build();
        }

        survey.delete();
        return Response.seeOther(UriBuilder.fromPath("/web/surveys").build()).build();
    }
}
