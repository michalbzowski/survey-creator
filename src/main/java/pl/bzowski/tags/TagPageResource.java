package pl.bzowski.tags;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;

@Path("/tags")
public class TagPageResource {

    private final Template addTag;
    private final Template listTags;

    public TagPageResource(Template addTag, Template listTags) {
        this.addTag = addTag;
        this.listTags = listTags;
    }

    @GET
    @Path("/add")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        return addTag.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addPerson(@FormParam("name") String name) {
        Tag tag = new Tag(name);
        tag.persist();
        return Response.seeOther(UriBuilder.fromPath("/tags/list").build()).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listTags() {
        List<Tag> tags = Tag.listAll();
        return listTags.data("tags", tags);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Person person = Person.findById(id);
            if (person != null) {
                person.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/persons/list").build()).build();
    }
}
