package pl.bzowski.attendance_list.api;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.attendance_list.service.AttendanceListRepository;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/attendance_list")
public class AttendanceListResource {

    private final AttendanceListRepository attendanceListRepository;

    @Inject
    public AttendanceListResource(AttendanceListRepository attendanceListRepository) {
        this.attendanceListRepository = attendanceListRepository;
    }

    @GET
    public List<AttendanceList> listAllAttendanceList() {
        return attendanceListRepository.listAll();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createAttendanceList(AttendanceListDTO attendanceListDTO) {
        try {
            AttendanceListDTO created = attendanceListRepository.createAttendanceList(attendanceListDTO);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteAttendanceList(@PathParam("id") UUID id) {
        AttendanceList attendanceList = AttendanceList.findById(id);
        if (attendanceList == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Query nie została znaleziona").build();
        }

        attendanceList.delete();
        return Response.seeOther(UriBuilder.fromPath("/web/attendance_list").build()).build();
    }
}
