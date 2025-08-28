package pl.bzowski.surveys.web;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pl.bzowski.links.PersonSurveyLink;
import pl.bzowski.surveys.Survey;

import java.util.List;
import java.util.UUID;

@Path("/web/surveys")
public class SurveyDetailsResource {

    private final Template surveyDetails;

    public SurveyDetailsResource(Template surveyDetails) {
        this.surveyDetails = surveyDetails;
    }

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showQueryDetails(@PathParam("id") UUID id) {
        Survey survey = Survey.findById(id);
        if (survey == null) {
            throw new NotFoundException("Nie znaleziono zapytania");
        }

        List<PersonSurveyLink> links = PersonSurveyLink.find("surveyId", Sort.by("personLastName"), survey.id).list();

        return surveyDetails
                .data("survey", survey)
                .data("links", links);
    }
}
