package pl.bzowski.links;

import io.smallrye.mutiny.Uni;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.logging.Level;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    @ConfigProperty(name = "app.host")
    String appHost;

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
        List<Person> persons = personRepository.listAll();
        if (generateLinksFor(attendanceListId, persons))
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();

        // Przekierowanie do widoku szczegółów zapytania
        return Response.seeOther(UriBuilder.fromPath("/web/attendance_list/{id}/details").build(attendanceListId)).build();
    }

    public boolean generateLinksFor(UUID attendanceListId, List<Person> persons) {
        logger.info("Generate links for " + attendanceListId.toString());
        AttendanceList attendanceList = AttendanceList.findById(attendanceListId);
        if (attendanceList == null) {
            logger.info("AttendanceList is null");
            return true;
        }

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
        return false;
    }

    @POST
    @Path("/{attendanceListId}/email/{personId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Uni<Void> sendLinkByEmail(@PathParam("attendanceListId") UUID attendanceListId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start sending link by email for attendanceList %s for person %s", attendanceListId, personId));
        AttendanceList attendanceList = AttendanceList.findById(attendanceListId);
        if (attendanceList == null) {
            logger.info("attendanceList is null");
            throw new NotFoundException("Zapytanie nie istnieje");
        }

        Person person = Person.findById(personId);
        if (person == null) {
            logger.info("Person is null");
            throw new NotFoundException("Osoba nie istnieje");
        }
        Optional<PersonAttendanceListLink> personAttendanceListLinkOptional = PersonAttendanceListLink.find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id).firstResultOptional();

        boolean exists = personAttendanceListLinkOptional.isPresent();
        if (!exists) {
            String format = String.format("Can not send link. Link doesn't exists for: %s - %s", person.email, attendanceListId);
            logger.info(format);
            throw new RuntimeException(format);
        } else {
            PersonAttendanceListLink personAttendanceListLink = personAttendanceListLinkOptional.get();
            String email = getEmailContent(personAttendanceListLink);
            try {
                Uni<Void> ret = emailService.sendEmail(person.email, "Czy będziesz na wydarzeniu?", email);
                logger.info(String.format("E-mail with link %s sent", email));
                personAttendanceListLink.sent();
                personAttendanceListLink.persist();
                return ret;
            } catch (RuntimeException ex) {
                String format = String.format("E-mail with link %s NOT SENT", email);
                personAttendanceListLink.sendingError();
                personAttendanceListLink.persist();
                logger.log(Level.WARNING, format);
                logger.log(Level.INFO, ex.getMessage());
                throw new RuntimeException();
            }
        }
    }

    private String getEmailContent(PersonAttendanceListLink personAttendanceListLink) {
        var lol = String.format("""
                <!DOCTYPE html>
                <html lang="pl">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Potwierdzenie obecności</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      background-color: #f9f9f9;
                      margin: 0; padding: 0;
                    }
                    .container {
                      max-width: 600px;
                      margin: 30px auto;
                      background-color: #ffffff;
                      padding: 20px;
                      border: 1px solid #ddd;
                      text-align: center;
                      color: #333333;
                    }
                    a {
                      color: #0078d7;
                      text-decoration: none;
                    }
                    a:hover {
                      text-decoration: underline;
                    }
                    h1 {
                      margin-bottom: 10px;
                    }
                    h2 {
                      color: #555555;
                      margin-bottom: 20px;
                    }
                    .button {
                      display: inline-block;
                      background-color: #0078d7;
                      color: white;
                      padding: 12px 25px;
                      border-radius: 5px;
                      font-weight: bold;
                      margin-bottom: 30px;
                      text-decoration: none;
                    }
                    .footer {
                      font-size: 0.9em;
                      color: #777777;
                      margin-top: 30px;
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <h1><a href="https://potwierdzobecnosc.pl" target="_blank" rel="noopener">PotwierdzObecnosc.pl</a></h1>
                
                    <h2>Prosimy o potwierdzenie obecności lub zgłoszenie nieobecności na wydarzeniu.</h2>
                    <p>%s</p>
                    <a href="%s" class="button" target="_blank" rel="noopener">Wypełnij ankietę</a>
                    <p>Ta wiadomość przeznaczona jest dla %s</p>
                    <p class="footer">Dziękuje za poświęcony czas i zaangażowanie!</p>
                  </div>
                </body>
                </html>
                
                """,
                personAttendanceListLink.attendanceList.joinedEventsName(),
                appHost + "/web/responses/" + personAttendanceListLink.linkToken.toString(),
                personAttendanceListLink.personEmail);
        return lol;

    }
}
