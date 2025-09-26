package pl.bzowski.attendances.list.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hibernate.reactive.mutiny.Mutiny;
import pl.bzowski.attendances.list.AttendanceList;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.events.Event;
import pl.bzowski.attendances.list.api.AttendanceListDTO;
import pl.bzowski.attendances.list.infrastructure.AttendanceListRepository;
import pl.bzowski.events.EventRepository;
import pl.bzowski.attendances.entry.AttendanceEntry;

import java.util.List;
import java.util.UUID;

@Path("/web/attendance_list")
public class AttendanceListPageResource {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    private final Template attendanceListDetails;
    private final Template createAttendanceList;
    private final Template listAttendanceList;
    private final AttendanceListRepository attendanceListRepository;
    private final JsonHelper jsonHelper;
    private final EventRepository eventRepository;

    public AttendanceListPageResource(Template attendanceListDetails, Template createAttendanceList, Template listAttendanceList, AttendanceListRepository attendanceListRepository, JsonHelper jsonHelper, EventRepository eventRepository) {
        this.attendanceListDetails = attendanceListDetails;
        this.createAttendanceList = createAttendanceList;
        this.listAttendanceList = listAttendanceList;
        this.attendanceListRepository = attendanceListRepository;
        this.jsonHelper = jsonHelper;
        this.eventRepository = eventRepository;
    }

    String query = "SELECT ae.id, ae.personId, ae.personFirstName, ae.personLastName, ae.personEmail, ae.attendanceListId, ae.linktoken, ae.attendancelistanswered, CASE WHEN cal.id IS NULL THEN FALSE ELSE TRUE END AS communicationSent " +
            "FROM person_attendance_list_links ae " +
            "LEFT JOIN communication_attendance_links cal ON ae.id = cal.attendanceEntryId " +
//            "LEFT JOIN communications c ON cal.attendanceentryid = c.id " +
            "WHERE ae.attendanceListId = :attendanceListId " +
            "ORDER BY ae.personLastName";

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> showQueryDetails(@PathParam("id") UUID id) {
        return AttendanceList.<AttendanceList>findById(id)
                .flatMap(attendanceList ->
                        sessionFactory.openSession()
                                .flatMap(session ->
                                        session.createNativeQuery(query, Tuple.class)
                                                .setParameter("attendanceListId", id)
                                                .getResultList()
                                                .map(list -> list.stream()
                                                        .map(this::getAttendanceEntryWithCommunicationDTO)
                                                        .toList())
                                )
                                .map(dtos -> attendanceListDetails
                                        .data("attendanceList", attendanceList)
                                        .data("links", dtos)
                                )
                );
    }

    private AttendanceEntryWithCommunicationDTO getAttendanceEntryWithCommunicationDTO(Tuple tuple) {
        return new AttendanceEntryWithCommunicationDTO(
                tuple.get(0, UUID.class),
                tuple.get(1, UUID.class),
                tuple.get(2, String.class),
                tuple.get(3, String.class),
                tuple.get(4, String.class),
                tuple.get(5, UUID.class),
                tuple.get(6, UUID.class),
                tuple.get(7, Boolean.class),
                tuple.get(8, Boolean.class)
        );
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> createAttendanceListForm(@QueryParam("name") String name, @QueryParam("eventId") UUID eventId) {
        return eventRepository.findAvailableEvents()
                .onItem()
                .transformToUni(availableEvents -> {
                    if (availableEvents.isEmpty()) {
                        return Uni.createFrom().failure(new RuntimeException("Stwórz wydarzenie!"));
                    }
                    List<Event> first = List.of(availableEvents.getFirst());
                    String availableEventsJson = jsonHelper.toJson(availableEvents);
                    return Uni.createFrom().item(
                            createAttendanceList.data("attendanceList", new AttendanceList("", first),
                                    "availableEvents", availableEvents,
                                    "availableEventsJson", availableEventsJson,
                                    "name", name,
                                    "eventId", eventId
                            )
                    );
                });
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @WithTransaction
    public Uni<Response> createAttendanceList(AttendanceListDTO attendanceList) {
        return attendanceListRepository.createAttendanceList(attendanceList)
                .onItem()
                .transformToUni(list -> Uni.createFrom().item(Response.ok(list).build()))
                .onFailure()
                .recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<TemplateInstance> listQueries() {
        return attendanceListRepository
                .listAllWithEvents()
                .onItem()
                .transform(attendanceLists -> listAttendanceList.data("attendanceList", attendanceLists));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> deletePersonDDD(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, AttendanceList::findById, "/web/attendance_list");
    }
}
