package pl.bzowski.persons;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/persons")
public class PersonPageResource {

    private final Template addPerson;
    private final Template listPersons;

    public PersonPageResource(Template addPerson, Template listPersons) {
        this.addPerson = addPerson;
        this.listPersons = listPersons;
    }

    @GET
    @Path("/add")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = Tag.listAll();
        return addPerson.data("tags", tags);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addPerson(@FormParam("firstName") String firstName,
                              @FormParam("lastName") String lastName,
                              @FormParam("email") String email,
                              @FormParam("defaultTag") String defaultTag
    ) {
        Person person = new Person(firstName, lastName, email, defaultTag);
        person.persist();
        return Response.seeOther(UriBuilder.fromPath("/persons/list").build()).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listPersons() {
        List<Person> persons = Person.listAll();
        return listPersons.data("persons", persons);
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
