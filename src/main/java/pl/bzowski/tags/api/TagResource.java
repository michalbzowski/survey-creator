package pl.bzowski.tags.api;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.TagsRepository;

@Path("/api/v1/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

    private final TagsRepository tagsRepository;

    @Inject
    public TagResource(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> createTag(@FormParam("name") String name) {
        return tagsRepository.createTag(name)
                .flatMap(p -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/tags").build()).build()));
    }
}
