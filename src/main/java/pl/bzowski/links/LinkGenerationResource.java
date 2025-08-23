package pl.bzowski.links;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.surveys.Survey;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinkGenerationResource {

    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    @GET
    public List<PersonSurveyLink> listAllLinks() {
        return PersonSurveyLink.listAll();
    }

    @GET
    @Path("/{surveyId}")
    @Transactional
    public Response generateLinks(@PathParam("surveyId") UUID surveyId) {
        logger.info("Generate links for " + surveyId.toString());
        Survey survey = Survey.findById(surveyId);
        if (survey == null) {
            logger.info("Survey is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        List<Person> persons = Person.listAll();
        logger.info("Persons found: " + (long) persons.size());
        for (Person person : persons) {
            // Sprawdź, czy link już istnieje
            Optional<PersonSurveyLink> personSurveyLink = PersonSurveyLink.find("person = ?1 and survey = ?2", person, survey).firstResultOptional();

            boolean exists = personSurveyLink.isPresent();
            if (!exists) {
                logger.info("Link doesn't exists for: " + person.email + " - " + surveyId);
                PersonSurveyLink link = new PersonSurveyLink(person, survey);
                link.persist();
            } else {
                logger.info("Person " + person.email + " has already link!");
            }
        }

        // Przekierowanie do widoku szczegółów zapytania
        return Response.seeOther(UriBuilder.fromPath("/web/surveys/{id}/details").build(surveyId)).build();
    }
}
