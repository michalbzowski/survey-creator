package pl.bzowski.message_template;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
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
    private final EventBus eventBus;

    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    @ConfigProperty(name = "app.host")
    String appHost;

    public LinkGenerationResource(EmailService emailService, PersonRepository personRepository, EventBus eventBus) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.eventBus = eventBus;
    }

    @GET
    public Uni<List<MessageTemplate>> listAllLinks() {
        return MessageTemplate.listAll();
    }

    @GET
    @Path("/{attendanceListId}")
    @WithTransaction
    public Uni<Response> generateLinks(@PathParam("attendanceListId") UUID attendanceListId) {
        return personRepository.listAll()
                .flatMap(persons -> generateMessageTemplateFor(attendanceListId, persons)
                        .flatMap(generated -> {
                            if (generated) {
                                return Uni.createFrom().item(
                                        Response.status(Response.Status.NOT_FOUND)
                                                .entity("Zapytanie nie istnieje")
                                                .build()
                                );
                            } else {
                                return Uni.createFrom().item(
                                        Response.seeOther(UriBuilder.fromPath("/web/attendance_list/{id}/details").build(attendanceListId))
                                                .build()
                                );
                            }
                        })
                );
    }

    public Uni<Boolean> generateMessageTemplateFor(UUID attendanceListId, List<Person> persons) {
        return AttendanceList.<AttendanceList>findById(attendanceListId)
                .flatMap(attendanceList -> {
                    if (attendanceList == null) {
                        logger.info("AttendanceList is null");
                        return Uni.createFrom().item(true);
                    }
                    logger.info("Persons found: " + persons.size());

                    // Dla każdego person sprawdzamy i potencjalnie tworzymy MessageTemplate
                    List<Uni<Void>> createLinksUnis = persons.stream()
                            .map(person -> MessageTemplate.find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id)
                                    .firstResult()
                                    .flatMap(existingTemplate -> {
                                        if (existingTemplate == null) {
                                            logger.info("Creating Message Template for: " + person.email + " - " + attendanceListId);
                                            MessageTemplate link = new MessageTemplate(person, attendanceList);
                                            return link.persistAndFlush().replaceWithVoid();
                                            // eventBus.publish(PERSIST_COMMUNICATION, link.toCommunicationDto()); // opcjonalnie w invoke()
                                        } else {
                                            logger.info("Person " + person.email + " already has message template for attendance list " + attendanceListId);
                                            return Uni.createFrom().voidItem();
                                        }
                                    })
                            ).toList();

                    return Uni.combine().all().unis(createLinksUnis).discardItems()
                            .map(i -> false);
                });
    }

    @POST
    @Path("/{attendanceListId}/send/{personId}")
    @WithTransaction
    public Uni<Void> saveAttendanceListMessageToPerson(@PathParam("attendanceListId") UUID attendanceListId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start saving message for attendanceList %s for person %s", attendanceListId, personId));

        return AttendanceList.<AttendanceList>findById(attendanceListId)
                .flatMap(attendanceList -> {
                    if (attendanceList == null) {
                        logger.info("attendanceList is null");
                        return Uni.createFrom().failure(new NotFoundException("Zapytanie nie istnieje"));
                    }
                    return Person.<Person>findById(personId)
                            .flatMap(person -> {
                                if (person == null) {
                                    logger.info("Person is null");
                                    return Uni.createFrom().failure(new NotFoundException("Osoba nie istnieje"));
                                }
                                return MessageTemplate.<MessageTemplate>find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id)
                                        .firstResult()
                                        .flatMap(messageTemplate -> {
                                            if (messageTemplate == null) {
                                                String format = String.format("Can not send link. Link doesn't exist for: %s - %s", person.email, attendanceListId);
                                                logger.info(format);
                                                return Uni.createFrom().failure(new RuntimeException(format));
                                            }
                                            String email = getEmailContent(messageTemplate);
                                            return emailService.sendEmail(person.email, "Czy będziesz na wydarzeniu?", email)
                                                    .onItem().invoke(() -> {
                                                        logger.info(String.format("Band member %s notified", person.email));
                                                        messageTemplate.sent();
                                                        messageTemplate.persistAndFlush().subscribe().with(__ -> {
                                                        });
                                                        logger.info(String.format("Finished saving message for attendanceList %s for person %s", attendanceListId, personId));
                                                    })
                                                    .onFailure().invoke(ex -> {
                                                        String format = String.format("E-mail with link %s NOT SENT", email);
                                                        messageTemplate.sendingError();
                                                        messageTemplate.persistAndFlush().subscribe().with(__ -> {
                                                        });
                                                        logger.log(Level.WARNING, format);
                                                        logger.log(Level.INFO, ex.getMessage());
                                                        throw new RuntimeException();
                                                    })
                                                    .replaceWithVoid();
                                        });
                            });
                });
    }

    private String getEmailContent(MessageTemplate messageTemplate) {
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
                messageTemplate.attendanceList.joinedEventsName(),
                appHost + "/web/responses/" + messageTemplate.linkToken.toString(),
                messageTemplate.personEmail);
        return lol;

    }
}
