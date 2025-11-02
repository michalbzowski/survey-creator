package pl.bzowski.events.web;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logmanager.Level;
import pl.bzowski.team.api.TeamDTO;
import pl.bzowski.team.infrastructure.TeamRepository;
import pl.bzowski.shared.base.ReactiveDelete;
import pl.bzowski.events.Event;
import pl.bzowski.events.EventRepository;
import pl.bzowski.groups.Group;
import pl.bzowski.groups.GroupsRepository;
import pl.bzowski.members.TeamCreatedDto;
import pl.bzowski.answers.Answer;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;
import pl.bzowski.team.web.TeamDetailsContext;
import pl.bzowski.team.web.TeamPageResource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static pl.bzowski.members.TeamCreatedDto.EVENT_CREATED;

@Path("/web/events")
public class EventsPageResource {

    Logger logger = Logger.getLogger(EventsPageResource.class.getName());

    private final Template createEvent;
    private final Template listEvents;
    private final Template eventDetails;
    private final TagsRepository tagsRepository;
    private final EventRepository eventRepository;
    private final GroupsRepository groupsRepository;
    private final PersonRepository personRepository;
    private final TeamRepository teamRepository;
    private final EventBus eventBus;
    private final TeamPageResource teamPageResource;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    public EventsPageResource(Template createEvent, Template listEvents, Template eventDetails, TagsRepository tagsRepository, EventRepository eventRepository, GroupsRepository groupsRepository, PersonRepository personRepository, TeamRepository teamRepository, EventBus eventBus, Mutiny.SessionFactory sessionFactory, TeamPageResource teamPageResource) {
        this.createEvent = createEvent;
        this.listEvents = listEvents;
        this.eventDetails = eventDetails;
        this.tagsRepository = tagsRepository;
        this.eventRepository = eventRepository;
        this.groupsRepository = groupsRepository;
        this.personRepository = personRepository;
        this.teamRepository = teamRepository;
        this.eventBus = eventBus;
        this.sessionFactory = sessionFactory;
        this.teamPageResource = teamPageResource;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> listEvents() {
        return eventRepository
                .listAll(Sort.by("localDateTime"))
                .flatMap(events -> Uni.createFrom().item(listEvents.data("events", events)));
    }

    private Uni<List<Tag>> loadTags() {
        return tagsRepository.listAll();
    }

    private Uni<List<Group>> loadGroups() {
        return groupsRepository.listAll();
    }

    private Uni<List<Person>> loadPersons() {
        return personRepository.listAll();
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<TemplateInstance> showAddForm() {
        return loadTags()
                .flatMap(tags -> loadGroups()
                        .flatMap(groups -> loadPersons()
                                .map(persons -> createEvent.data(
                                        "tags", tags,
                                        "groups", groups,
                                        "persons", persons
                                ))));
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> createEvent(@BeanParam EventDto eventDto,
                                     @FormParam("withTeam") String withTeam,
                                     @FormParam("teamType") String teamType,
                                     @FormParam("groups") List<UUID> groupIds,
                                     @FormParam("persons") List<UUID> personIds) {
        return validateMembers(withTeam, teamType, groupIds, personIds)
                .onItem()
                .transformToUni(b -> eventRepository.persist(eventDto)
                        .invoke(withTeamLogger(withTeam))
                        .call(eventIfWithTeamChecked(withTeam, teamType, groupIds, personIds))
                        .map(redirectToEventDetails())
                        .onFailure()
                        .invoke(logFailure())
                        .onFailure()
                        .recoverWithItem(returnServerError()))
                .onFailure()
                .recoverWithItem(e -> Response.status(400).entity(e.getMessage()).build());
    }

    private static Function<Event, Response> redirectToEventDetails() {
        return event -> Response.seeOther(UriBuilder.fromPath("/web/events/" + event.id + "/details").build()).build();
    }

    private Function<Event, Uni<?>> eventIfWithTeamChecked(String withTeam, String teamType, List<UUID> groupIds, List<UUID> personIds) {
        return event -> {
            if ("checked".equals(withTeam)) {
                return teamRepository.createTeam(new TeamDTO(event.id))
                        .onItem()
                        .invoke(publish(withTeam, teamType, groupIds, personIds, event.registeredUserId))
                        .onFailure()
                        .invoke(failure -> System.out.println(failure.getMessage()));
            }
            return Uni.createFrom().voidItem();
        };
    }

    @POST
    @Path("/members")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> addMembers(@FormParam("withTeam") String withTeam,
                                    @FormParam("teamType") String teamType,
                                    @FormParam("groups") List<UUID> groupIds,
                                    @FormParam("persons") List<UUID> personIds,
                                    @FormParam("teamId") UUID teamId,
                                    @FormParam("eventId") UUID eventId) {
        return validateMembers(withTeam, teamType, groupIds, personIds)
                .onItem()
                .transformToUni(v -> {
                    if (teamId == null) {
                        return eventRepository.findById(eventId)
                                .call(eventIfWithTeamChecked(withTeam, teamType, groupIds, personIds))
                                .onItem()
                                .transformToUni(t -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/events/{id}/details")
                                        .build(eventId)).build()));

                    } else {
                        if ("checked".equals(withTeam)) {

                            return teamRepository.currentRegisteredUserId()
                                    .onItem()
                                    .invoke(registeredUserId -> {
                                        eventBus.publish(EVENT_CREATED,
                                                new TeamCreatedDto(withTeam, teamType, groupIds, personIds, teamId, registeredUserId));
                                    })
                                    .onItem()
                                    .transformToUni(t -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/events/{id}/details")
                                            .build(eventId)).build()));

                        }
                    }
                    return Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/events/{id}/details")
                            .build(eventId)).build());
                })
                .onFailure(NotFoundException.class)
                .recoverWithItem(e -> Response.status(400).entity(e.getMessage()).build());
    }

    private Uni<Boolean> validateMembers(String withTeam, String teamType, List<UUID> groupIds, List<UUID> personIds) {
        if ("checked".equals(withTeam)) {
            if ("group".equals(teamType) && groupIds.isEmpty()) {
                return Uni.createFrom().failure(new NotFoundException("Nie wybrałeś żadnej grupy"));
            }
            if ("person".equals(teamType) && personIds.isEmpty()) {
                return Uni.createFrom().failure(new NotFoundException("Nie wybrałeś żadnej osoby z listy osób"));
            }
            if ("all".equals(teamType)) {
                return personRepository.hasCurrentUserAnyPerson()
                        .onItem()
                        .transform(hasPerson -> {
                            if (!hasPerson) {
                                throw new NotFoundException("Nie masz zapisanej żadnej osoby");
                            }
                            return true;
                        });
            }
        }
        return Uni.createFrom().item(true); // Walidacja OK (nie ma błędu)
    }

    private Consumer<TeamDTO> publish(String withTeam, String
            teamType, List<UUID> groupIds, List<UUID> personIds, UUID registeredUserId) {
        return teamDTO -> {
            eventBus.publish(EVENT_CREATED,
                    new TeamCreatedDto(withTeam, teamType, groupIds, personIds, teamDTO.id, registeredUserId));
        };
    }

    private Consumer<Event> withTeamLogger(String withTeam) {
        return event -> logger.info("Created event withTeam: " + withTeam);
    }

    private static Function<Throwable, Response> returnServerError() {
        return throwable -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to create event: " + throwable.getMessage())
                .build();
    }

    private Consumer<Throwable> logFailure() {
        return ex -> logger.log(Level.ERROR, "Failed to create event", ex);
    }

    private final String query_0 = """
            select e.id as event_id, e.team_id as team_id, e.name as event_name,
            		e.location as event_location, e.localdatetime as event_localdatetime,
            		e.description as event_description, count(m.id) as members_count,
            		count(cal.id) as sent_links,
            		count(a_tak.id) as tak, count(a_nie.id) as nie, count(a_pozniej.id) as pozniej
            from public.events e
            left join public.members m on m.teamId = e.team_id
            left JOIN public.communication_team_links cal ON m.id = cal.teamEntryId
            left join public.answers a_tak on a_tak.event_Id = e.id and a_tak.team_id = e.team_id and m.id = a_tak.member_id and a_tak.answerValue = 'TAK'
            left join public.answers a_nie on a_nie.event_Id = e.id and a_nie.team_id = e.team_id and m.id = a_nie.member_id and a_nie.answerValue = 'NIE'
            left join public.answers a_pozniej on a_pozniej.event_Id = e.id and a_pozniej.team_id = e.team_id and m.id = a_pozniej.member_id and a_pozniej.answerValue = 'ODPOWIEM_POZNIEJ'
            where e.id = :eventId
            group by e.id, e.team_id
            """;

    @GET
    @Path("/{eventId}/details")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> eventDetails(@PathParam("eventId") UUID eventId) {
        //TODO: Refactor that - make one view to get all data for that page
        return findEventById(eventId)
                .flatMap(this::loadStats)
                .flatMap(ctx -> teamPageResource.getCreateTeamData(ctx.teamId)
                        .onItem()
                        .transform(teamData -> ctx.teamData = teamData)
                        .onItem()
                        .transform(_ -> ctx))
                .map(this::buildTemplateInstance);
    }

    private Uni<EventContext> findEventById(UUID eventId) {
        return sessionFactory.openSession()
                .flatMap(session ->
                        session.createNativeQuery(query_0, Tuple.class)
                                .setParameter("eventId", eventId)
                                .getResultList()

                ).onItem()
                .transformToUni(list -> list.stream()
                        .map(a -> {
//                                UUID eventId = UUID.fromString(a.get("event_id").toString());
                            UUID teamId = getTeamId(a);
                            String eventName = (String) a.get("event_name");
                            String eventLocation = (String) a.get("event_location");
                            LocalDateTime eventLocaldatetime = (LocalDateTime) a.get("event_localdatetime");
                            String eventDescription = (String) a.get("event_description");
                            Long membersCount = (Long) a.get("members_count");
                            Long sentLinks = (Long) a.get("sent_links");
                            Long tak = (Long) a.get("tak");
                            Long nie = (Long) a.get("nie");
                            Long pozniej = (Long) a.get("pozniej");
                            return Uni.createFrom().item(new EventContext(eventId, teamId, eventName, eventLocation, eventLocaldatetime, eventDescription,
                                    membersCount, sentLinks, tak, nie, pozniej));
                        }).findFirst().get());
    }

    private static UUID getTeamId(Tuple a) {
        Object teamId = a.get("team_id");
        if (teamId != null) {
            return UUID.fromString(teamId.toString());
        } else {
            return null;
        }
    }

    String query = "SELECT count(cal.id)" +
            "FROM members m " +
            "JOIN communication_team_links cal ON m.id = cal.teamEntryId " + //IF cal is present, then assuming communication was sent? //TODO: add another join and check is SEND status for real eg //  "LEFT JOIN communications c ON cal.teamentryid = c.id " + ?
            "WHERE m.teamId = :teamId ";

    private Uni<EventContext> loadStats(EventContext ctx) {
        // Sekwencyjne ładowanie statystyk odpowiedzi według tagów
        return teamRepository.currentRegisteredUserId()
                .flatMap(registeredUserId -> getResultListReactive(ctx.eventId, Answer.AnswerValue.TAK, registeredUserId)
                        .flatMap(takStats -> getResultListReactive(ctx.eventId, Answer.AnswerValue.NIE, registeredUserId)
                                .flatMap(nieStats -> getResultListReactive(ctx.eventId, Answer.AnswerValue.ODPOWIEM_POZNIEJ, registeredUserId)
                                        .map(laterStats -> {
                                            ctx.fullStats = combineStats(takStats, nieStats, laterStats);
                                            return ctx;
                                        }))));

    }

    private List<EventDetails.Stats> combineStats(List<Object[]> takStats, List<Object[]> nieStats, List<Object[]>
            laterStats) {
        List<EventDetails.Stats> fullStats = new ArrayList<>();
        for (Object[] takStat : takStats) {
            String tag = (String) takStat[0];
            Long yes = (Long) takStat[1];
            Long no = nieStats.stream()
                    .filter(o -> o[0].equals(tag))
                    .findFirst()
                    .map(o -> (Long) o[1])
                    .orElse(0L);
            Long later = laterStats.stream()
                    .filter(o -> o[0].equals(tag))
                    .findFirst()
                    .map(o -> (Long) o[1])
                    .orElse(0L);
            fullStats.add(new EventDetails.Stats(tag, yes, no, later));
        }
        return fullStats;
    }

    private TemplateInstance buildTemplateInstance(EventContext ctx) {
        long answerSum = ctx.tak + ctx.nie + ctx.pozniej;
        long unansweredCount = ctx.membersCount - answerSum;

        return eventDetails
                .data("eventId", ctx.eventId)
                .data("eventName", ctx.eventName)
                .data("eventLocation", ctx.eventLocation)
                .data("eventLocaldatetime", ctx.eventLocaldatetime)
                .data("eventDescription", ctx.eventDescription)
                .data("linkCount", ctx.membersCount)
                .data("sentLinkCount", ctx.sentLinks)
                .data("answerSum", answerSum)
                .data("unansweredCount", unansweredCount)
                .data("answerYesCount", ctx.tak)
                .data("answerNoCount", ctx.nie)
                .data("answerLaterCount", ctx.pozniej)
                .data("fullStats", ctx.fullStats)
                .data("noTeamYet", ctx.noTeamYet)
                .data("eventTeamId", ctx.teamId)
                .data("team", ctx.teamData.getTeam())
                .data("links", ctx.teamData.getLinks())
                .data("groups", ctx.teamData.getGroups())
                .data("persons", ctx.teamData.getPersons());
    }

    private static class EventContext {
        final boolean noTeamYet;
        final UUID teamId;
        private final String eventName;
        private final String eventLocation;
        private final LocalDateTime eventLocaldatetime;
        private final String eventDescription;
        private final Long membersCount;
        private final Long sentLinks;
        private final Long tak;
        private final Long nie;
        private final Long pozniej;
        private final UUID eventId;
        TeamDetailsContext teamData;

        List<EventDetails.Stats> fullStats;

        public EventContext(UUID eventId, UUID teamId, String eventName, String eventLocation, LocalDateTime eventLocaldatetime, String eventDescription, Long membersCount, Long sentLinks, Long tak, Long nie, Long pozniej) {
            this.eventId = eventId;
            this.teamId = teamId;
            this.noTeamYet = teamId == null;
            this.eventName = eventName;
            this.eventLocation = eventLocation;
            this.eventLocaldatetime = eventLocaldatetime;
            this.eventDescription = eventDescription;
            this.membersCount = membersCount;
            this.sentLinks = sentLinks;
            this.tak = tak;
            this.nie = nie;
            this.pozniej = pozniej;
        }
    }


    private Uni<List<Object[]>> getResultListReactive(UUID eventId, Answer.AnswerValue answerValue, UUID registeredUserId) {
        return Panache
                .getSession()
                .onItem()
                .transformToUni(session -> session.createQuery(
                                "SELECT t.name, COUNT(a.member) " +
                                        "FROM Tag t " +
                                        "LEFT JOIN Member m ON m.personTag = t.name " +
                                        "LEFT JOIN Answer a ON a.member = m AND a.event.id = :eventId AND a.answerValue = :answerValue " +
                                        "WHERE t.registeredUserId = :registeredUserId " +
                                        "GROUP BY t.name", Object[].class)
                        .setParameter("eventId", eventId)
                        .setParameter("answerValue", answerValue)
                        .setParameter("registeredUserId", registeredUserId)
                        .getResultList());
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> deleteEvent(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, Event::findById, "/web/events");
    }


}
