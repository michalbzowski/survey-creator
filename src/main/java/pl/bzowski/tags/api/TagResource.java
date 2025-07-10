package pl.bzowski.tags.api;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.Tag;

@Path("/api/v1/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addPerson(@FormParam("name") String name) {
        Tag tag = new Tag(name);
        tag.persist();
        return Response.seeOther(UriBuilder.fromPath("/web/tags").build()).build();
    }
}
