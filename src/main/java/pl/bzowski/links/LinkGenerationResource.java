package pl.bzowski.links;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.surveys.Survey;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinkGenerationResource {

    @GET
    public List<PersonSurveyLink> listAllLinks() {
        return PersonSurveyLink.listAll();
    }

    @GET
    @Path("/{surveyId}")
    @Transactional
    public Response generateLinks(@PathParam("surveyId") UUID surveyId) {
        Survey survey = Survey.findById(surveyId);
        if (survey == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        List<Person> persons = Person.listAll();
        for (Person person : persons) {
            // Sprawdź, czy link już istnieje
            boolean exists = PersonSurveyLink.find("person = ?1 and survey = ?2", person, survey).firstResultOptional().isPresent();
            if (!exists) {
                PersonSurveyLink link = new PersonSurveyLink(person, survey);
                link.persist();
            }
        }

        // Przekierowanie do widoku szczegółów zapytania
        return Response.seeOther(UriBuilder.fromPath("/web/surveys/{id}/details").build(surveyId)).build();

    }
}
