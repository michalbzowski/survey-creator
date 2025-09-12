package pl.bzowski.tags.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;

import java.util.List;
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
    public TemplateInstance showAddForm() {
        return addTag.instance();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTags() {
        List<Tag> tags = tagsRepository.listAll();
        return listTags.data("tags", tags);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Tag tag = Tag.findById(id);
            if (tag != null) {
                tag.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/web/tags").build()).build();
    }
}
