package pl.bzowski.persons.web;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.persons.Person;
import pl.bzowski.tags.Tag;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Path("/web/persons")
public class PersonPageResource {

    private final Template addPerson;
    private final Template listPersons;

    public PersonPageResource(Template addPerson, Template listPersons) {
        this.addPerson = addPerson;
        this.listPersons = listPersons;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = Tag.listAll();
        return addPerson.data("person", new Person(), "tags", tags);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addPerson(@FormParam("firstName") String firstName,
                              @FormParam("lastName") String lastName,
                              @FormParam("email") String email,
                              @FormParam("defaultTag") String defaultTag
    ) {
        Tag tag = Tag.find("name", defaultTag).firstResult();
        Person person = new Person(firstName, lastName, email, tag);
        person.persist();
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listPersons() {
        List<Person> persons = Person.listAll(Sort.by("lastName"));
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
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showEditForm(@PathParam("id") UUID id) {
        Person person = Person.findById(id);
        if (person == null) {
            throw new WebApplicationException("Person not found", 404);
        }
        var tags = Tag.listAll();
        return addPerson.data("person", person)
                .data("tags", tags)
                .data("edit", true);  // Flaga, by zmienić formularz z dodawania na edycję
    }

    @POST
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response editPerson(@PathParam("id") UUID id,
                               @FormParam("firstName") String firstName,
                               @FormParam("lastName") String lastName,
                               @FormParam("email") String email,
                               @FormParam("defaultTag") String defaultTag) {
        Person person = Person.findById(id);
        if (person == null) {
            throw new WebApplicationException("Person not found", 404);
        }
        Tag tag = Tag.find("name", defaultTag).firstResult();
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;
        person.defaultTag = tag;
        // person.persist() nie jest potrzebne, bo entita jest zarządzana (transakcja)
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

}
