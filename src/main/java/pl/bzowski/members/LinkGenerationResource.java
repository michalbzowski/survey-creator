package pl.bzowski.members;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.bzowski.team.Team;
import pl.bzowski.messaging.*;
import pl.bzowski.messaging.email.MemberAssignedMailSentEvent;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.bzowski.messaging.CommunicationEventListener.PERSIST_COMMUNICATION;
import static pl.bzowski.messaging.email.MemberAssignedMailSender.MEMBER_ASSIGNED_MAIL_SENT;

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
    @Path("/{teamId}")
    @WithTransaction
    public Uni<Response> generateLinks(@PathParam("teamId") UUID teamId) {
        return personRepository.listAll()
                .onItem()
                .transformToUni(persons -> processTeam(teamId, persons))
                .onItem()
                .transformToUni(d -> Uni.createFrom().item(
                        getBuild(Response.seeOther(
                                UriBuilder.fromPath("/web/teams/{id}/details")
                                        .build(teamId)
                        ))
                ))
                .onFailure()
                .recoverWithItem(d ->
                        getBuild(Response.status(Response.Status.NOT_FOUND)
                                .entity("Zapytanie nie istnieje")));
    }

    private static Response getBuild(Response.ResponseBuilder teamId) {
        return teamId.build();
    }


    @WithTransaction
    public Uni<Void> processTeam(UUID teamId, List<Person> persons) {
        return Team.<Team>findById(teamId)
                .onFailure().invoke(_ -> logger.info("Failure - team id: " + teamId))
                .onItem().ifNull().failWith(() -> new WebApplicationException("team not found", 404))
                .flatMap(team -> {
                    logger.info("Found team with id: " + teamId);

                    return Multi.createFrom().iterable(persons)
                            .onItem().transformToUniAndConcatenate(person ->
                                    Member.find("personId = ?1 and teamId = ?2", person.id, team.id)
                                            .firstResult()
                                            .flatMap(messageTemplate -> {
                                                if (messageTemplate != null) {
                                                    logger.info(String.format("Found existing Member for Person %s and Team %s.", person.id, team.id));
                                                    return Uni.createFrom().voidItem();
                                                } else {
                                                    logger.info(String.format("Creating new Member for Person %s and Team %s.", person.id, team.id));
                                                    Member member = new Member(person, team);
                                                    Uni<PanacheEntityBase> persist = member.persist();
                                                    return persist.replaceWithVoid();
                                                }
                                            })
                            )
                            .collect().asList()
                            .call(list -> {
                                logger.info("List size: " + list.stream().count());
                                return Uni.createFrom().voidItem();
                            })
                            .replaceWithVoid();

                });
    }

    @POST
    @Path("/{teamId}/send/{personId}")
    @WithTransaction
    public Uni<Void> saveteamMessageToPerson(@PathParam("teamId") UUID
                                                     teamId, @PathParam("personId") UUID personId) {
        logger.info(String.format("Start saving message for team %s for person %s", teamId, personId));

        return personRepository.registeredUserId().flatMap(currentUserId -> {
            return Team.<Team>findById(teamId)
                    .flatMap(team -> {
                        if (team == null) {
                            logger.info("team is null");
                            return Uni.createFrom().failure(new NotFoundException("Zapytanie nie istnieje"));
                        }
                        return Person.<Person>findById(personId)
                                .flatMap(person -> {
                                    if (person == null) {
                                        logger.info("Person is null");
                                        return Uni.createFrom().failure(new NotFoundException("Osoba nie istnieje"));
                                    }
                                    return Member.<Member>find("personId = ?1 and teamId = ?2", person.id, team.id)
                                            .firstResult()
                                            .flatMap(member -> {
                                                if (member == null) {
                                                    String format = String.format("Can not send link. Link doesn't exist for: %s - %s", person.email, teamId);
                                                    logger.info(format);
                                                    return Uni.createFrom().failure(new RuntimeException(format));
                                                }
                                                Map<String, Object> properties = Map.of("eventTitle", member.team.joinedEventsName(),
                                                        "appHost", appHost,
                                                        "teamEntryLink", appHost + "/web/responses/" + member.linkToken.toString(),
                                                        "personEmail", member.personEmail,
                                                        "teamEntryId", member.id);
                                                this.eventBus.publish(PERSIST_COMMUNICATION, new PersistCommunicationCommand(Channel.EMAIL, CommunicationTemplate.TEAM_RECORD_LINK, currentUserId, person, properties));
                                                return Uni.createFrom().voidItem();
                                            });
                                });
                    });
        });
    }

    @Path("/{teamEntryId}/status")
    @WithTransaction
    @GET
    public Uni<Response> getStatus(@PathParam("teamEntryId") UUID teamId) {
        logger.info("teamEntryId:" + teamId.toString());
        return CommunicationTeamLink.<CommunicationTeamLink>find("teamEntryId = ?1 ", teamId)
                .firstResult()
                .flatMap(cal -> Communication
                        .<Communication>findById(cal.communicationId)
                        .flatMap(comm -> {
                            SendingStatus status = comm.getStatus();
                            logger.info("KOT: " + status.name());
                            Response build = getBuild(Response.ok(Map.of("status", status)));
                            return Uni.createFrom().item(build);
                        }))
                .onFailure()
                .recoverWithItem(a -> {
                    logger.log(Level.FINEST, a.toString());
                    logger.info("PIES: " + SendingStatus.TO_SEND.name());
                    return getBuild(Response.ok(Map.of("status", SendingStatus.TO_SEND)));
                });
    }


    @ConsumeEvent(MEMBER_ASSIGNED_MAIL_SENT)
    public void lol(Message<MemberAssignedMailSentEvent> msg) {
        MemberAssignedMailSentEvent body = msg.body();
        costamService.persist(body).subscribe().with(l -> System.out.println("Lot"));

    }
}
