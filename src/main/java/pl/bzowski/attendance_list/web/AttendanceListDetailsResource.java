package pl.bzowski.attendance_list.web;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.links.PersonAttendanceListLink;

import java.util.List;
import java.util.UUID;

@Path("/web/attendance_list")
public class AttendanceListDetailsResource {

    private final Template attendanceListDetails;

    public AttendanceListDetailsResource(Template attendanceListDetails) {
        this.attendanceListDetails = attendanceListDetails;
    }

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showQueryDetails(@PathParam("id") UUID id) {
        AttendanceList attendanceList = AttendanceList.findById(id);
        if (attendanceList == null) {
            throw new NotFoundException("Nie znaleziono zapytania");
        }

        List<PersonAttendanceListLink> links = PersonAttendanceListLink.find("attendanceListId", Sort.by("personLastName"), attendanceList.id).list();

        return attendanceListDetails
                .data("attendanceList", attendanceList)
                .data("links", links);
    }
}
