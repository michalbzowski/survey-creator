package pl.bzowski.tags.api;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.Tag;
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
    @Transactional
    public Response createTag(@FormParam("name") String name) {
        tagsRepository.createTag(name);
        return Response.seeOther(UriBuilder.fromPath("/web/tags").build()).build();
    }
}
