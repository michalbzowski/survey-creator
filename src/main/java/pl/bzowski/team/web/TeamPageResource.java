package pl.bzowski.team.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hibernate.reactive.mutiny.Mutiny;
import pl.bzowski.team.Team;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.events.Event;
import pl.bzowski.team.api.TeamDTO;
import pl.bzowski.team.infrastructure.TeamRepository;
import pl.bzowski.events.EventRepository;

import java.util.List;
import java.util.UUID;

@Path("/web/teams")
public class TeamPageResource {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    private final Template teamDetails;
    private final Template createTeam;
    private final Template listTeam;
    private final TeamRepository teamRepository;
    private final JsonHelper jsonHelper;
    private final EventRepository eventRepository;

    public TeamPageResource(Template teamDetails, Template createTeam, Template listTeam, TeamRepository teamRepository, JsonHelper jsonHelper, EventRepository eventRepository) {
        this.teamDetails = teamDetails;
        this.createTeam = createTeam;
        this.listTeam = listTeam;
        this.teamRepository = teamRepository;
        this.jsonHelper = jsonHelper;
        this.eventRepository = eventRepository;
    }

    String query = "SELECT ae.id, ae.personId, ae.personFirstName, ae.personLastName, ae.personEmail, ae.teamId, ae.linktoken, ae.teamanswered, CASE WHEN cal.id IS NULL THEN FALSE ELSE TRUE END AS communicationSent " +
            "FROM team_member ae " +
            "LEFT JOIN communication_team_links cal ON ae.id = cal.teamEntryId " +
//            "LEFT JOIN communications c ON cal.teamentryid = c.id " +
            "WHERE ae.teamId = :teamId " +
            "ORDER BY ae.personLastName";

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> showQueryDetails(@PathParam("id") UUID id) {
        return Team.<Team>findById(id)
                .flatMap(team ->
                        sessionFactory.openSession()
                                .flatMap(session ->
                                        session.createNativeQuery(query, Tuple.class)
                                                .setParameter("teamId", id)
                                                .getResultList()
                                                .map(list -> list.stream()
                                                        .map(this::getTeamEntryWithCommunicationDTO)
                                                        .toList())
                                )
                                .map(dtos -> teamDetails
                                        .data("team", team)
                                        .data("links", dtos)
                                )
                );
    }

    private TeamEntryWithCommunicationDTO getTeamEntryWithCommunicationDTO(Tuple tuple) {
        return new TeamEntryWithCommunicationDTO(
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
    public Uni<TemplateInstance> createteamForm(@QueryParam("name") String name, @QueryParam("eventId") UUID eventId) {
        return eventRepository.findAvailableEvents()
                .onItem()
                .transformToUni(availableEvents -> {
                    if (availableEvents.isEmpty()) {
                        return Uni.createFrom().failure(new RuntimeException("Stwórz wydarzenie!"));
                    }
                    List<Event> first = List.of(availableEvents.getFirst());
                    String availableEventsJson = jsonHelper.toJson(availableEvents);
                    return Uni.createFrom().item(
                            createTeam.data("team", new Team("", first),
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
    public Uni<Response> createteam(TeamDTO team) {
        return teamRepository.createTeam(team)
                .onItem()
                .transformToUni(list -> Uni.createFrom().item(Response.ok(list).build()))
                .onFailure()
                .recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<TemplateInstance> listQueries() {
        return teamRepository
                .listAllWithEvents()
                .onItem()
                .transform(teams -> listTeam.data("team", teams));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> deletePersonDDD(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, Team::findById, "/web/teams");
    }
}
