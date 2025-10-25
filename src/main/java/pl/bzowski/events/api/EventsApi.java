package pl.bzowski.events.api;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import pl.bzowski.team.infrastructure.TeamRepository;

import java.util.UUID;

@Path("/api/events")
public class EventsApi {

    @Inject
    private TeamRepository teamRepository;

    @GET
    @Path("/{teamId}/hasMembers")
    @WithSession
    public Uni<Boolean> teamHasMembers(@PathParam("teamId") UUID teamId) {
        return teamRepository.teamHasMembers(teamId);
    }
}
