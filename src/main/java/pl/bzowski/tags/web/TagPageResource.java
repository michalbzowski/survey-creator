package pl.bzowski.tags.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.TagsRepository;

import java.util.UUID;

@Path("/web/tags")
public class TagPageResource {

    private final Template addTag;
    private final Template listTags;
    private final TagsRepository tagsRepository;

    public TagPageResource(Template addTag, Template listTags, TagsRepository tagsRepository) {
        this.addTag = addTag;
        this.listTags = listTags;
        this.tagsRepository = tagsRepository;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> showAddForm() {
        return Uni.createFrom().item(addTag.instance());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> listTags() {
        return tagsRepository.listAll().flatMap(tags -> Uni.createFrom().item(listTags.data("tags", tags)));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> deleteTag(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if (!"delete".equalsIgnoreCase(method)) {
            return Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/tags").build()).build());
        } else {
            return tagsRepository.deleteTag(id)
                    .flatMap(conflictingPersons -> {
                        if (conflictingPersons.isEmpty()) {
                            return Uni.createFrom().item(Response.ok().build());
                        } else {
                            return Uni.createFrom().item(
                                    Response.status(Response.Status.CONFLICT)
                                            .entity(conflictingPersons)
                                            .build()
                            );
                        }
                    });
        }
    }
}
