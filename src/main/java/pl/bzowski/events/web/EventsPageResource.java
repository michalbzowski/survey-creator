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
import pl.bzowski.attendances.entry.CommunicationAttendanceLink;
import pl.bzowski.attendances.list.api.AttendanceListDTO;
import pl.bzowski.attendances.list.infrastructure.AttendanceListRepository;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.messaging.SendingStatus;
import pl.bzowski.events.Event;
import pl.bzowski.events.EventRepository;
import pl.bzowski.groups.Group;
import pl.bzowski.groups.GroupsRepository;
import pl.bzowski.attendances.entry.AttendanceCreatedDto;
import pl.bzowski.attendances.entry.AttendanceEntry;
import pl.bzowski.events.PersonEventAnswer;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static pl.bzowski.attendances.entry.AttendanceCreatedDto.EVENT_WITH_ATTENDANCE_CREATED;

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
    private final AttendanceListRepository attendanceListRepository;
    private final EventBus eventBus;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    public EventsPageResource(Template createEvent, Template listEvents, Template eventDetails, TagsRepository tagsRepository, EventRepository eventRepository, GroupsRepository groupsRepository, PersonRepository personRepository, AttendanceListRepository attendanceListRepository, EventBus eventBus, Mutiny.SessionFactory sessionFactory) {
        this.createEvent = createEvent;
        this.listEvents = listEvents;
        this.eventDetails = eventDetails;
        this.tagsRepository = tagsRepository;
        this.eventRepository = eventRepository;
        this.groupsRepository = groupsRepository;
        this.personRepository = personRepository;
        this.attendanceListRepository = attendanceListRepository;
        this.eventBus = eventBus;
        this.sessionFactory = sessionFactory;
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
                                     @FormParam("withAttendanceList") String withAttendanceList,
                                     @FormParam("attendanceType") String attendanceType,
                                     @FormParam("groups") List<UUID> groupIds,
                                     @FormParam("persons") List<UUID> personIds) {
        return eventRepository.persist(eventDto)
                .invoke(withAttendanceListLogger(withAttendanceList))
                .call(eventIfWithAttendanceListChecked(eventDto, withAttendanceList, attendanceType, groupIds, personIds))
                .map(redirectToEventDetails())
                .onFailure().invoke(logFailure())
                .onFailure().recoverWithItem(returnServerError());
    }

    private static Function<Event, Response> redirectToEventDetails() {
        return event -> Response.seeOther(UriBuilder.fromPath("/web/events/" + event.id + "/details").build()).build();
    }

    private Function<Event, Uni<?>> eventIfWithAttendanceListChecked(EventDto eventDto, String withAttendanceList, String attendanceType, List<UUID> groupIds, List<UUID> personIds) {
        return event -> {
            if ("checked".equals(withAttendanceList)) {
                return attendanceListRepository.createAttendanceList(new AttendanceListDTO(event.id))
                        .onItem()
                        .invoke(publish(eventDto, withAttendanceList, attendanceType, groupIds, personIds, event));
            }
            return Uni.createFrom().voidItem();
        };
    }

    private Consumer<AttendanceListDTO> publish(EventDto eventDto, String withAttendanceList, String attendanceType, List<UUID> groupIds, List<UUID> personIds, Event event) {
        return attendanceListDTO -> {
            eventBus.publish(EVENT_WITH_ATTENDANCE_CREATED,
                    new AttendanceCreatedDto(withAttendanceList, attendanceType, groupIds, personIds, attendanceListDTO.id, event.registeredUserId, eventDto));
        };
    }

    private Consumer<Event> withAttendanceListLogger(String withAttendanceList) {
        return event -> logger.info("Created event withAttendanceList: " + withAttendanceList);
    }

    private static Function<Throwable, Response> returnServerError() {
        return throwable -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to create event: " + throwable.getMessage())
                .build();
    }

    private Consumer<Throwable> logFailure() {
        return ex -> logger.log(Level.ERROR, "Failed to create event", ex);
    }

    private Uni<AttendanceListDTO> persistAttendanceList(EventDto eventDto, Event event) {
        // Utwórz listę obecności (AttendanceList) powiązaną z tym wydarzeniem
        AttendanceListDTO attendanceListDTO = new AttendanceListDTO();
        attendanceListDTO.name = eventDto.name;
        attendanceListDTO.events = List.of(event.id);
        return attendanceListRepository.createAttendanceList(attendanceListDTO);
    }

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> eventDetails(@PathParam("id") UUID id) {
        return findEventById(id)
                .flatMap(this::loadLinkCount)
                .flatMap(this::loadSentLinkCount)
                .flatMap(this::loadAnswerYesCount)
                .flatMap(this::loadAnswerNoCount)
                .flatMap(this::loadAnswerLaterCount)
                .flatMap(this::loadStats)
                .map(this::buildTemplateInstance);
    }

    private Uni<EventContext> findEventById(UUID id) {
        return Event.<Event>findById(id)
                .onItem().ifNull().failWith(() -> new WebApplicationException("Event not found", 404))
                .map(event -> new EventContext(event));
    }

    private Uni<EventContext> loadLinkCount(EventContext ctx) {
        if (ctx.noAttendanceListYet) {
            ctx.linkCount = 0L;
            return Uni.createFrom().item(ctx);
        }
        return AttendanceEntry.count("attendanceListId = ?1", ctx.attendanceListId)
                .map(count -> {
                    ctx.linkCount = count;
                    return ctx;
                });
    }

    String query = "SELECT count(cal.id)" +
            "FROM person_attendance_list_links ae " +
            "JOIN communication_attendance_links cal ON ae.id = cal.attendanceEntryId " + //IF cal is present, then assuming communication was sent? //TODO: add another join and check is SEND status for real eg //  "LEFT JOIN communications c ON cal.attendanceentryid = c.id " + ?
            "WHERE ae.attendanceListId = :attendanceListId ";

    private Uni<EventContext> loadSentLinkCount(EventContext ctx) {
        if (ctx.noAttendanceListYet) {
            ctx.sentLinkCount = 0L;
            return Uni.createFrom().item(ctx);
        }
        return sessionFactory.openSession()
                .flatMap(session ->
                        session.createNativeQuery(query, Tuple.class)
                                .setParameter("attendanceListId", ctx.attendanceListId)
                                .getResultList()

                )
                .map(tuple -> {
                    ctx.sentLinkCount = tuple.getFirst().get(0, Long.class);
                    return ctx;
                });
    }

    private Uni<EventContext> loadAnswerYesCount(EventContext ctx) {
        return PersonEventAnswer.count("event = ?1 and answer = ?2", ctx.event, PersonEventAnswer.Answer.TAK)
                .map(count -> {
                    ctx.answerYesCount = count;
                    return ctx;
                });
    }

    private Uni<EventContext> loadAnswerNoCount(EventContext ctx) {
        return PersonEventAnswer.count("event = ?1 and answer = ?2", ctx.event, PersonEventAnswer.Answer.NIE)
                .map(count -> {
                    ctx.answerNoCount = count;
                    return ctx;
                });
    }

    private Uni<EventContext> loadAnswerLaterCount(EventContext ctx) {
        return PersonEventAnswer.count("event = ?1 and answer = ?2", ctx.event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ)
                .map(count -> {
                    ctx.answerLaterCount = count;
                    return ctx;
                });
    }

    private Uni<EventContext> loadStats(EventContext ctx) {
        // Sekwencyjne ładowanie statystyk odpowiedzi według tagów
        return getResultListReactive(ctx.event, PersonEventAnswer.Answer.TAK).flatMap(takStats -> {
            return getResultListReactive(ctx.event, PersonEventAnswer.Answer.NIE).flatMap(nieStats -> {
                return getResultListReactive(ctx.event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ).map(laterStats -> {
                    ctx.fullStats = combineStats(takStats, nieStats, laterStats);
                    return ctx;
                });
            });
        });
    }

    private List<EventDetails.Stats> combineStats(List<Object[]> takStats, List<Object[]> nieStats, List<Object[]> laterStats) {
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
        long answerSum = ctx.answerYesCount + ctx.answerNoCount + ctx.answerLaterCount;
        long unansweredCount = ctx.linkCount - answerSum;

        return eventDetails
                .data("event", ctx.event)
                .data("linkCount", ctx.linkCount)
                .data("sentLinkCount", ctx.sentLinkCount)
                .data("answerSum", answerSum)
                .data("unansweredCount", unansweredCount)
                .data("answerYesCount", ctx.answerYesCount)
                .data("answerNoCount", ctx.answerNoCount)
                .data("answerLaterCount", ctx.answerLaterCount)
                .data("fullStats", ctx.fullStats)
                .data("noAttendanceListYet", ctx.noAttendanceListYet)
                .data("eventAttendanceListId", ctx.attendanceListId);
    }

    private static class EventContext {
        final Event event;
        final boolean noAttendanceListYet;
        final UUID attendanceListId;

        long linkCount;
        long sentLinkCount;
        long answerYesCount;
        long answerNoCount;
        long answerLaterCount;
        List<EventDetails.Stats> fullStats;

        EventContext(Event event) {
            this.event = event;
            this.noAttendanceListYet = event.attendanceList == null;
            this.attendanceListId = noAttendanceListYet ? null : event.attendanceList.id;
        }
    }


    private Uni<List<Object[]>> getResultListReactive(Event event, PersonEventAnswer.Answer answer) {
        return Panache
                .getSession()
                .onItem()
                .transformToUni(session -> session.createQuery(
                                "SELECT t.name, COUNT(pea.person) " +
                                        "FROM Tag t " +
                                        "LEFT JOIN Person p ON p.defaultTag = t " +
                                        "LEFT JOIN PersonEventAnswer pea ON pea.person = p AND pea.event = :event AND pea.answer = :answer " +
                                        "GROUP BY t.name", Object[].class)
                        .setParameter("event", event)
                        .setParameter("answer", answer)
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
