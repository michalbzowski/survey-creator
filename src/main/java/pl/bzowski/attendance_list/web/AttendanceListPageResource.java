package pl.bzowski.attendance_list.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.events.Event;
import pl.bzowski.attendance_list.api.AttendanceListDTO;
import pl.bzowski.attendance_list.service.AttendanceListService;

import java.util.List;
import java.util.UUID;

@Path("/web/attendance_list")
public class AttendanceListPageResource {

    private final Template createAttendanceList;
    private final Template listAttendanceList;
    private final AttendanceListService attendanceListService;
    private final JsonHelper jsonHelper;

    public AttendanceListPageResource(Template createAttendanceList, Template listAttendanceList, AttendanceListService attendanceListService, JsonHelper jsonHelper) {
        this.createAttendanceList = createAttendanceList;
        this.listAttendanceList = listAttendanceList;
        this.attendanceListService = attendanceListService;
        this.jsonHelper = jsonHelper;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createAttendanceListForm(@QueryParam("name") String name, @QueryParam("eventId") UUID eventId) {
        List<Event> availableEvents = Event.findAvailableEvents();
        List<Event> first = List.of(availableEvents.getFirst());
        String availableEventsJson = jsonHelper.toJson(availableEvents);
        return createAttendanceList.data("attendanceList", new AttendanceList("", first),
                "availableEvents", availableEvents,
                "availableEventsJson", availableEventsJson,
                "name", name,
                "eventId", eventId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createAttendanceList(AttendanceListDTO attendanceList) {
        try {
            var dto = attendanceListService.createAttendanceList(attendanceList);
            return Response.ok(dto).build();
        } catch (IllegalArgumentException e) {
            // obsługa błędu, np. zwrócenie strony z komunikatem
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listQueries() {
        List<AttendanceList> attendanceList = AttendanceList.listAll();
        return listAttendanceList.data("attendanceList", attendanceList);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            AttendanceList attendanceList = AttendanceList.findById(id);
            if (attendanceList != null) {
                attendanceList.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/web/attendance_list").build()).build();
    }
}
