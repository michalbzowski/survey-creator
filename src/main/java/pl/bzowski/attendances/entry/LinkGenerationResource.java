package pl.bzowski.attendances.entry;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.bzowski.attendances.list.AttendanceList;
import pl.bzowski.messaging.*;
import pl.bzowski.messaging.email.EmailAttendanceEntryLinkSentEvent;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.bzowski.messaging.CommunicationEventListener.PERSIST_COMMUNICATION;
import static pl.bzowski.messaging.email.EmailAttendanceEntryLinkSender.EMAIL_ATTENDANCE_ENTRY_LINK_SENT;

@Path("/api/v1/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinkGenerationResource {


    private final PersonRepository personRepository;
    private final EventBus eventBus;
    private final CostamService costamService;

    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    @ConfigProperty(name = "app.host")
    String appHost;


    public LinkGenerationResource(PersonRepository personRepository, EventBus eventBus, CostamService costamService) {
        this.personRepository = personRepository;
        this.eventBus = eventBus;
        this.costamService = costamService;
    }

    @GET
    public Uni<List<AttendanceEntry>> listAllLinks() {
        return AttendanceEntry.listAll();
    }

    @GET
    @Path("/{attendanceListId}")
    @WithTransaction
    public Uni<Response> generateLinks(@PathParam("attendanceListId") UUID attendanceListId) {
        return personRepository.listAll()
                .flatMap(persons -> processAttendanceList(attendanceListId, persons)
                        .onItem()
                        .transformToUni(d -> Uni.createFrom().item(
                                Response.seeOther(UriBuilder.fromPath("/web/attendance_list/{id}/details").build(attendanceListId))
                                        .build()
                        ))
                        .onFailure()
                        .recoverWithItem(d ->
                                Response.status(Response.Status.NOT_FOUND)
                                        .entity("Zapytanie nie istnieje")
                                        .build()));
    }


    public Uni<Void> processAttendanceList(UUID attendanceListId, List<Person> persons) {
        return AttendanceList.<AttendanceList>findById(attendanceListId)
                .onItem().ifNull().failWith(() -> new WebApplicationException("AttendanceList not found", 404))
                .flatMap(attendanceList -> {
                    logger.info("Found AttendanceList with id: " + attendanceListId);

                    return Multi.createFrom().iterable(persons)
                            .onItem().transformToUniAndConcatenate(person ->
                                    AttendanceEntry.find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id)
                                            .firstResult()
                                            .flatMap(messageTemplate -> {
                                                if (messageTemplate != null) {
                                                    logger.info("Found existing MessageTemplate for person: " + person.id);
                                                    return Uni.createFrom().voidItem();
                                                } else {
                                                    logger.info("Creating new MessageTemplate for person: " + person.id);
                                                    AttendanceEntry newTemplate = new AttendanceEntry(person, attendanceList);
                                                    return newTemplate.persist()
                                                            .replaceWithVoid();
                                                }
                                            })
                            )
                            .collect().last()
                            .replaceWithVoid();

                });
    }

    @POST
    @Path("/{attendanceListId}/send/{personId}")
    @WithTransaction
    public Uni<Void> saveAttendanceListMessageToPerson(@PathParam("attendanceListId") UUID
                                                               attendanceListId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start saving message for attendanceList %s for person %s", attendanceListId, personId));

        return personRepository.currentUserId().flatMap(currentUserId -> {
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
                                    return AttendanceEntry.<AttendanceEntry>find("personId = ?1 and attendanceListId = ?2", person.id, attendanceList.id)
                                            .firstResult()
                                            .flatMap(attendanceEntry -> {
                                                if (attendanceEntry == null) {
                                                    String format = String.format("Can not send link. Link doesn't exist for: %s - %s", person.email, attendanceListId);
                                                    logger.info(format);
                                                    return Uni.createFrom().failure(new RuntimeException(format));
                                                }
                                                Map<String, Object> properties = Map.of("eventTitle", attendanceEntry.attendanceList.joinedEventsName(),
                                                        "appHost", appHost,
                                                        "attendanceEntryLink", appHost + "/web/responses/" + attendanceEntry.linkToken.toString(),
                                                        "personEmail", attendanceEntry.personEmail,
                                                        "attendanceEntryId", attendanceEntry.id);
                                                this.eventBus.publish(PERSIST_COMMUNICATION, new PersistCommunicationCommand(Channel.EMAIL, CommunicationTemplate.ATTENDANCE_RECORD_LINK, currentUserId, person, properties));
                                                return Uni.createFrom().voidItem();
                                            });
                                });
                    });
        });
    }

    @Path("/{attendanceEntryId}/status")
    @WithTransaction
    @GET
    public Uni<Response> getStatus(@PathParam("attendanceEntryId") UUID attendanceListId) {
        logger.info("attendanceEntryId:" + attendanceListId.toString());
        return CommunicationAttendanceLink.<CommunicationAttendanceLink>find("attendanceEntryId = ?1 ", attendanceListId)
                .firstResult()
                .flatMap(cal -> Communication
                        .<Communication>findById(cal.communicationId)
                        .flatMap(comm -> {
                            SendingStatus status = comm.getStatus();
                            logger.info("KOT: " + status.name());
                            Response build = Response.ok(Map.of("status", status)).build();
                            return Uni.createFrom().item(build);
                        }))
                .onFailure()
                .recoverWithItem(a -> {
                    logger.log(Level.FINEST, a.toString());
                    logger.info("PIES: " + SendingStatus.TO_SEND.name());
                    return Response.ok(Map.of("status", SendingStatus.TO_SEND)).build();
                });
    }


    @ConsumeEvent(EMAIL_ATTENDANCE_ENTRY_LINK_SENT)
    public void lol(Message<EmailAttendanceEntryLinkSentEvent> msg) {
        EmailAttendanceEntryLinkSentEvent body = msg.body();
        costamService.persiste(body).subscribe().with(l -> System.out.println("Lot"));

    }
}
