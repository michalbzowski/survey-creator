package pl.bzowski.events.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.events.Event;
import pl.bzowski.tags.Tag;

import java.util.List;

@Path("/web/events")
public class EventsPageResource {

    private final Template addEvent;
    private final Template listEvents;

    public EventsPageResource(Template addEvent, Template listEvents) {
        this.addEvent = addEvent;
        this.listEvents = listEvents;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listEvents() {
        List<Event> events = Event.listAll();
        return listEvents.data("events", events);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = Tag.listAll();
        return addEvent.data("tags", tags);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addEvent(@BeanParam EventDto eventDto) {
        Event event = new Event(eventDto.name, eventDto.location, eventDto.localDateTime, eventDto.description);
        event.persist();
        return Response.seeOther(UriBuilder.fromPath("/web/events").build()).build();
    }

}
