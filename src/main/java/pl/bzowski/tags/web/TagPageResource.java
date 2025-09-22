package pl.bzowski.tags.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.persons.Person;
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
    public Uni<TemplateInstance> listTags() {
        return tagsRepository.listAll().flatMap(tags ->
                (Uni<? extends TemplateInstance>) listTags.data("tags", tags));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Uni<Response> deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, Person::findById, "/web/tags");
    }
}
