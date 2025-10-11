package pl.bzowski.team.list.api;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.bzowski.team.list.Team;
import pl.bzowski.team.list.infrastructure.TeamRepository;
import pl.bzowski.team.entry.LinkGenerationResource;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/api/v1/teams")
public class TeamResources {

    Logger logger = Logger.getLogger(LinkGenerationResource.class.getName());

    private final TeamRepository teamRepository;

    @Inject
    public TeamResources(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GET
    public Uni<List<Team>> listAllteam() {
        return teamRepository.listAll();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createteam(TeamDTO teamDTO) {
        logger.info("createteam");
        return teamRepository.createTeam(teamDTO)
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
    public Uni<Response> deleteteam(@PathParam("id") UUID id) {
        return Team.deleteById(id)
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
