package pl.bzowski.events.web;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.attendance_list.api.AttendanceListDTO;
import pl.bzowski.attendance_list.infrastructure.AttendanceListRepository;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.communication.SendingStatus;
import pl.bzowski.events.Event;
import pl.bzowski.events.EventRepository;
import pl.bzowski.group.GroupsRepository;
import pl.bzowski.message_template.LinkGenerationResource;
import pl.bzowski.message_template.MessageTemplate;
import pl.bzowski.events.PersonEventAnswer;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.tags.TagsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/web/events")
public class EventsPageResource {

    Logger logger = Logger.getLogger(EventsPageResource.class.getName());

    private final Template addEvent;
    private final Template listEvents;
    private final Template eventDetails;
    private final TagsRepository tagsRepository;
    private final EventRepository eventRepository;
    private final GroupsRepository groupsRepository;
    private final PersonRepository personRepository;
    private final AttendanceListRepository attendanceListRepository;
    private final LinkGenerationResource linkGenerationResource;

    public EventsPageResource(Template addEvent, Template listEvents, Template eventDetails, TagsRepository tagsRepository, EventRepository eventRepository, GroupsRepository groupsRepository, PersonRepository personRepository, AttendanceListRepository attendanceListRepository, LinkGenerationResource linkGenerationResource) {
        this.addEvent = addEvent;
        this.listEvents = listEvents;
        this.eventDetails = eventDetails;
        this.tagsRepository = tagsRepository;
        this.eventRepository = eventRepository;
        this.groupsRepository = groupsRepository;
        this.personRepository = personRepository;
        this.attendanceListRepository = attendanceListRepository;
        this.linkGenerationResource = linkGenerationResource;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> listEvents() {
        return eventRepository
                .listAll(Sort.by("localDateTime"))
                .flatMap(
                        events -> (Uni<? extends TemplateInstance>) listEvents.data("events", events));
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = tagsRepository.listAll();
        var groups = groupsRepository.listAll();
        var persons = personRepository.listAll();
        return addEvent.data("tags", tags,
                "groups", groups,
                "persons", persons);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> addEvent(@BeanParam EventDto eventDto,
                                  @FormParam("withAttendanceList") String withAttendanceList,
                                  @FormParam("attendanceType") String attendanceType,
                                  @FormParam("groups") List<UUID> groupIds,
                                  @FormParam("persons") List<UUID> personIds) {
        return eventRepository.persist(eventDto)
                .flatMap(event -> {
                    logger.info("withAttendanceList: " + withAttendanceList);
                    if ("checked".equals(withAttendanceList)) {
                        return persistAttendanceList(eventDto, event)
                                .flatMap(attendanceList -> {

                                    Uni<Boolean> generateLinksUni;

                                    switch (attendanceType) {
                                        case "group":
                                            if (groupIds != null && !groupIds.isEmpty()) {
                                                generateLinksUni = getPersonsFromGroups(groupIds)
                                                        .flatMap(personsFromGroups -> linkGenerationResource.generateMessageTemplateFor(attendanceList.id, personsFromGroups));
                                            } else {
                                                generateLinksUni = Uni.createFrom().item(false);
                                            }
                                            break;
                                        case "person":
                                            if (personIds != null && !personIds.isEmpty()) {
                                                generateLinksUni = getSelectedPersonsFromForm(personIds)
                                                        .flatMap(selectedPersons -> {
                                                            logger.info("Kot: " + selectedPersons.size());
                                                            return linkGenerationResource.generateMessageTemplateFor(attendanceList.id, selectedPersons);
                                                        });
                                            } else {
                                                generateLinksUni = Uni.createFrom().item(false);
                                            }
                                            break;
                                        case "all":
                                            generateLinksUni = personRepository.listAll()
                                                    .flatMap(allPersons -> linkGenerationResource.generateMessageTemplateFor(attendanceList.id, allPersons));
                                            break;
                                        default:
                                            generateLinksUni = Uni.createFrom().item(false);
                                    }

                                    return generateLinksUni
                                            .map(generated ->
                                                    Response.seeOther(UriBuilder.fromPath("/web/attendance_list/" + attendanceList.id + "/details").build())
                                                            .build());
                                });
                    } else {
                        return Uni.createFrom().item(
                                Response.seeOther(UriBuilder.fromPath("/web/events/" + event.id + "/details").build())
                                        .build()
                        );
                    }
                });
    }


    private static Uni<List<Person>> getSelectedPersonsFromForm(List<UUID> personIds) {
        return Person.find("id in ?1", personIds).list();
    }

    private static Uni<List<Person>> getPersonsFromGroups(List<UUID> groupIds) {
        return Person.find("select p from Person p join p.groups g where g.id in ?1", groupIds).list();
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
        logger.info("Kot:" + id.toString());
        return Event.<Event>findById(id)
                .onItem().ifNull().failWith(() -> new WebApplicationException("Event not found", 404))
                .onItem().transformToUni(event -> {
                    boolean noAttendanceListYet = event.attendanceList == null;
                    UUID attendanceListId = noAttendanceListYet ? null : event.attendanceList.id;

                    Uni<Long> linkCount = noAttendanceListYet ?
                            Uni.createFrom().item(0L) :
                            MessageTemplate.count("attendanceListId = ?1", attendanceListId);

                    Uni<Long> sentLinkCount = noAttendanceListYet ?
                            Uni.createFrom().item(0L) :
                            MessageTemplate.count("attendanceListId = ?1 and status = ?2", attendanceListId, SendingStatus.SENT);

                    Uni<Long> answerYesCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, PersonEventAnswer.Answer.TAK);
                    Uni<Long> answerNoCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, PersonEventAnswer.Answer.NIE);
                    Uni<Long> answerLaterCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ);

                    return Uni.combine().all().unis(linkCount, sentLinkCount, answerYesCount, answerNoCount, answerLaterCount)
                            .asTuple()
                            .onItem().transformToUni(tuple -> {
                                long linkCountVal = tuple.getItem1();
                                long sentLinkCountVal = tuple.getItem2();
                                long answerYesCountVal = tuple.getItem3();
                                long answerNoCountVal = tuple.getItem4();
                                long answerLaterCountVal = tuple.getItem5();

                                long answerSum = answerYesCountVal + answerNoCountVal + answerLaterCountVal;
                                long unansweredCount = linkCountVal - answerSum;

                                PersonEventAnswer.Answer tak = PersonEventAnswer.Answer.TAK;

                                Uni<List<Object[]>> takStatsUni = getResultListReactive(event, tak);
                                Uni<List<Object[]>> nieStatsUni = getResultListReactive(event, PersonEventAnswer.Answer.NIE);
                                Uni<List<Object[]>> laterStatsUni = getResultListReactive(event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ);

                                return Uni.combine().all().unis(takStatsUni, nieStatsUni, laterStatsUni)
                                        .asTuple()
                                        .onItem().transform(tupleStats -> {
                                            List<Object[]> takStats = tupleStats.getItem1();
                                            List<Object[]> nieStats = tupleStats.getItem2();
                                            List<Object[]> laterStats = tupleStats.getItem3();

                                            record Stats(String tag, Long yes, Long no, Long later) {
                                            }

                                            List<Stats> fullStats = new ArrayList<>();
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
                                                fullStats.add(new Stats(tag, yes, no, later));
                                            }

                                            // Zakładam, że eventDetails jest polem TemplateInstance dostępnego w klasie
                                            return eventDetails
                                                    .data("event", event)
                                                    .data("linkCount", linkCountVal)
                                                    .data("sentLinkCount", sentLinkCountVal)
                                                    .data("answerSum", answerSum)
                                                    .data("unansweredCount", unansweredCount)
                                                    .data("answerYesCount", answerYesCountVal)
                                                    .data("answerNoCount", answerNoCountVal)
                                                    .data("answerLaterCount", answerLaterCountVal)
                                                    .data("fullStats", fullStats)
                                                    .data("noAttendanceListYet", noAttendanceListYet)
                                                    .data("eventAttendanceListId", attendanceListId);
                                        });
                            });
                });
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
