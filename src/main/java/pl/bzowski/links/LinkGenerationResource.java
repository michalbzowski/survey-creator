package pl.bzowski.links;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.logging.Level;

import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.email.EmailService;
import pl.bzowski.persons.PersonRepository;
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
    public List<PersonAttendanceListLink> listAllLinks() {
        return PersonAttendanceListLink.listAll();
    }

    @GET
    @Path("/{attendanceListId}")
    @Transactional
    public Response generateLinks(@PathParam("attendanceListId") UUID attendanceListId) {
        logger.info("Generate links for " + attendanceListId.toString());
        AttendanceList attendanceList = AttendanceList.findById(attendanceListId);
        if (attendanceList == null) {
            logger.info("AttendanceList is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        List<Person> persons = personRepository.listAll();
        logger.info("Persons found: " + (long) persons.size());
        for (Person person : persons) {
            // Sprawdź, czy link już istnieje
            Optional<PersonAttendanceListLink> personAttendanceListLink = PersonAttendanceListLink.find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id).firstResultOptional();

            boolean exists = personAttendanceListLink.isPresent();
            if (!exists) {
                logger.info("Link doesn't exists for: " + person.email + " - " + attendanceListId);
                PersonAttendanceListLink link = new PersonAttendanceListLink(person, attendanceList);
                link.persist();
            } else {
                logger.info("Person " + person.email + " has already link!");
            }
        }

        // Przekierowanie do widoku szczegółów zapytania
        return Response.seeOther(UriBuilder.fromPath("/web/attendance_list/{id}/details").build(attendanceListId)).build();
    }

    @POST
    @Path("/{attendanceListId}/email/{personId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response sendLinkByEmail(@PathParam("attendanceListId") UUID attendanceListId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start sending link by email for attendanceList %s for person %s", attendanceListId, personId));
        AttendanceList attendanceList = AttendanceList.findById(attendanceListId);
        if (attendanceList == null) {
            logger.info("attendanceList is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        Person person = Person.findById(personId);
        if (person == null) {
            logger.info("Person is null");
            return Response.status(Response.Status.NOT_FOUND).entity("Osoba nie istnieje").build();
        }
        Optional<PersonAttendanceListLink> personAttendanceListLinkOptional = PersonAttendanceListLink.find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id).firstResultOptional();

        boolean exists = personAttendanceListLinkOptional.isPresent();
        if (!exists) {
            logger.info(String.format("Can not send link. Link doesn't exists for: %s - %s", person.email, attendanceListId));
            return Response.status(Response.Status.NOT_FOUND).entity("Link nie istnieje").build();
        } else {
            PersonAttendanceListLink personAttendanceListLink = personAttendanceListLinkOptional.get();
            String wholeLink = "http://localhost:8080" + "/web/responses/" + personAttendanceListLink.linkToken.toString();
            try {
                emailService.sendEmail(person.email, "new mail", wholeLink);
                logger.info(String.format("E-mail with link %s sent", wholeLink));
                personAttendanceListLink.sent();
                personAttendanceListLink.persist();
                String redirectUrl = String.format("/web/attendance_list/%s/details", attendanceListId.toString()); // adres strony, na którą chcesz wrócić
                return Response.seeOther(URI.create(redirectUrl)).build();
            } catch (RuntimeException ex) {
                String format = String.format("E-mail with link %s NOT SENT", wholeLink);
                personAttendanceListLink.sendingError();
                personAttendanceListLink.persist();
                logger.log(Level.WARNING, format);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Błąd!").build();
            }
        }
    }
}
