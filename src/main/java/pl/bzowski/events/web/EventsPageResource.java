package pl.bzowski.events.web;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.events.Event;
import pl.bzowski.events.EventRepository;
import pl.bzowski.links.PersonAttendanceListLink;
import pl.bzowski.events.PersonEventAnswer;
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

    public EventsPageResource(Template addEvent, Template listEvents, Template eventDetails, TagsRepository tagsRepository, EventRepository eventRepository) {
        this.addEvent = addEvent;
        this.listEvents = listEvents;
        this.eventDetails = eventDetails;
        this.tagsRepository = tagsRepository;
        this.eventRepository = eventRepository;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listEvents() {
        List<Event> events = eventRepository.listAll(Sort.by("localDateTime"));
        return listEvents.data("events", events);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = tagsRepository.listAll();
        return addEvent.data("tags", tags);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addEvent(@BeanParam EventDto eventDto) {
        Event event = eventRepository.persist(eventDto);

        return Response.seeOther(UriBuilder.fromPath("/web/events/" + event.id + "/details").build()).build();
    }

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance eventDetails(@PathParam("id") UUID id) {
        logger.info("Kot:" + id.toString());
        Event event = Event.findById(id);
        if (event == null) {
            throw new WebApplicationException("Event not found", 404);
        }


        // Ładujemy statystyki
        boolean noAttendanceListYet = event.attendanceList == null;
        long linkCount = event.attendanceList == null ? 0 : PersonAttendanceListLink.count("attendanceListId = ?1", event.attendanceList.id);
        long sentLinkCount = PersonAttendanceListLink.count("attendanceListId = ?1 and status = ?2", event.attendanceList.id, PersonAttendanceListLink.SendingStatus.SENT);
        PersonEventAnswer.Answer tak = PersonEventAnswer.Answer.TAK;
        long answerYesCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, tak);
        long answerNoCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, PersonEventAnswer.Answer.NIE);
        long answerLaterCount = PersonEventAnswer.count("event = ?1 and answer = ?2", event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ);
        long answerSum = answerYesCount + answerNoCount + answerLaterCount;
        long unansweredCount = linkCount - answerSum;

        // Zapytanie do grupowania tagów z liczbą osób z tym tagiem z odpowiedziami TAK
        record Stats(String tag, Long yes, Long no, Long later) {

        }
        List<Object[]> takStats = getResultList(event, tak);
        List<Object[]> nieStats = getResultList(event, PersonEventAnswer.Answer.NIE);
        List<Object[]> laterStats = getResultList(event, PersonEventAnswer.Answer.ODPOWIEM_POZNIEJ);

        List<Stats> fullStats = new ArrayList<>();
        for (Object[] takStat : takStats) {
            String tag = (String) takStat[0];
            Long yes = (Long) takStat[1];
            Long no = (Long) nieStats.stream().filter(o -> o[0].equals(tag)).findFirst().get()[1];
            Long later = (Long) laterStats.stream().filter(o -> o[0].equals(tag)).findFirst().get()[1];
            fullStats.add(new Stats(tag, yes, no, later));
        }

        return eventDetails
                .data("event", event)
                .data("linkCount", linkCount)
                .data("sentLinkCount", sentLinkCount)
                .data("answerSum", answerSum)
                .data("unansweredCount", unansweredCount)
                .data("answerYesCount", answerYesCount)
                .data("answerNoCount", answerNoCount)
                .data("answerLaterCount", answerLaterCount)
                .data("fullStats", fullStats)
                .data("noAttendanceListYet", noAttendanceListYet)
                .data("eventAttendanceListId", event.attendanceList == null ? null : event.attendanceList.id);
    }

    private static List<Object[]> getResultList(Event event, PersonEventAnswer.Answer answer) {
        return Panache.getEntityManager()
                .createQuery(
                        "SELECT t.name, COUNT(pea.person) " +
                                "FROM Tag t " +
                                "LEFT JOIN Person p ON p.defaultTag = t " +
                                "LEFT JOIN PersonEventAnswer pea ON pea.person = p AND pea.event = :event AND pea.answer = :answer " +
                                "GROUP BY t.name", Object[].class)
                .setParameter("event", event)
                .setParameter("answer", answer)
                .getResultList();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deleteEvent(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Event event = Event.findById(id);
            if (event != null) {
                event.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/web/events").build()).build();
    }


}
