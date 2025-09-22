package pl.bzowski.attendance_list.api;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.attendance_list.infrastructure.AttendanceListRepository;
import pl.bzowski.message_template.LinkGenerationResource;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/api/v1/attendance_list")
public class AttendanceListResource {

    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    private final AttendanceListRepository attendanceListRepository;

    @Inject
    public AttendanceListResource(AttendanceListRepository attendanceListRepository) {
        this.attendanceListRepository = attendanceListRepository;
    }

    @GET
    public Uni<List<AttendanceList>> listAllAttendanceList() {
        return attendanceListRepository.listAll();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createAttendanceList(AttendanceListDTO attendanceListDTO) {
        logger.info("createAttendanceList");
        return attendanceListRepository.createAttendanceList(attendanceListDTO)
                .onItem()
                .transform(created ->
                        Response.status(Response.Status.CREATED).entity(created).build()
                )
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(e ->
                        Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build()
                );
    }


    @DELETE //Response.status(Response.Status.NOT_FOUND).build()
    @Path("/{id}")
    @WithTransaction
    public Uni<Response> deleteAttendanceList(@PathParam("id") UUID id) {
        return AttendanceList.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Failed to delete: " + throwable.getMessage())
                                .build());
    }
}
