package pl.bzowski.links;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.logging.Level;
import pl.bzowski.email.EmailService;
import pl.bzowski.persons.PersonRepository;
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

    private final EmailService emailService;
    private final PersonRepository personRepository;
    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    public LinkGenerationResource(EmailService emailService, PersonRepository personRepository) {
        this.emailService = emailService;
        this.personRepository = personRepository;
    }

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

        List<Person> persons = personRepository.listAll();
        logger.info("Persons found: " + (long) persons.size());
        for (Person person : persons) {
            // Sprawdź, czy link już istnieje
            Optional<PersonSurveyLink> personSurveyLink = PersonSurveyLink.find("personId = ?1 and surveyId = ?2", person.id, survey.id).firstResultOptional();

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

    @POST
    @Path("/{surveyId}/email/{personId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response sendLinkByEmail(@PathParam("surveyId") UUID surveyId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start sending link by email for survey %s for person %s", surveyId, personId));
        Survey survey = Survey.findById(surveyId);
        if (survey == null) {
            logger.info("Survey is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        Person person = Person.findById(personId);
        if (person == null) {
            logger.info("Person is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Osoba nie istnieje").build();
        }
        Optional<PersonSurveyLink> personSurveyLinkOptional = PersonSurveyLink.find("personId = ?1 and surveyId = ?2", person.id, survey.id).firstResultOptional();

        boolean exists = personSurveyLinkOptional.isPresent();
        if (!exists) {
            logger.info(String.format("Can not send link. Link doesn't exists for: %s - %s", person.email, surveyId));
            return Response.status(Response.Status.NOT_FOUND).entity("Link nie istnieje").build();
        } else {
            PersonSurveyLink personSurveyLink = personSurveyLinkOptional.get();
            String wholeLink = "http://localhost:8080" + "/web/responses/" + personSurveyLink.linkToken.toString();
            try {
                emailService.sendEmail(person.email, "new mail", wholeLink);
                logger.info(String.format("E-mail with link %s sent", wholeLink));
                personSurveyLink.sent();
                personSurveyLink.persist();
                String redirectUrl = String.format("/web/surveys/%s/details", surveyId.toString()); // adres strony, na którą chcesz wrócić
                return Response.seeOther(URI.create(redirectUrl)).build();
            } catch (RuntimeException ex) {
                String format = String.format("E-mail with link %s NOT SENT", wholeLink);
                personSurveyLink.sendingError();
                personSurveyLink.persist();
                logger.log(Level.WARNING, format);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Błąd!").build();
            }
        }
    }
}
