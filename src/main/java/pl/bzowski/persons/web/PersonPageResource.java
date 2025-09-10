package pl.bzowski.persons.web;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.group.Group;
import pl.bzowski.group.GroupCreateRequest;
import pl.bzowski.group.GroupsRepository;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("/web/persons")
public class PersonPageResource {

    private final Template addPerson;
    private final Template listPersons;
    private final TagsRepository tagsRepository;
    private final PersonRepository personRepository;
    private final GroupsRepository groupsRepository;

    public PersonPageResource(Template addPerson, Template listPersons, TagsRepository tagsRepository, PersonRepository personRepository, GroupsRepository groupsRepository) {
        this.addPerson = addPerson;
        this.listPersons = listPersons;
        this.tagsRepository = tagsRepository;
        this.personRepository = personRepository;
        this.groupsRepository = groupsRepository;
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        var tags = tagsRepository.listAll();
        var groups = groupsRepository.listAll();
        return addPerson.data(
                "person", new Person(),
                "tags", tags,
                "groups", groups);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addPerson(@FormParam("firstName") String firstName,
                              @FormParam("lastName") String lastName,
                              @FormParam("email") String email,
                              @FormParam("defaultTag") String defaultTag,
                              @FormParam("groups") List<UUID> groupsIds
    ) {
        Tag tag = Tag.find("name", defaultTag).firstResult();
        Person person = new Person(firstName, lastName, email, tag);
        addGroupsToPerson(groupsIds, person);
        personRepository.persist(person);
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listPersons() {
        List<Person> persons = personRepository.listAll(Sort.by("lastName"));
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
        var tags = tagsRepository.listAll();
        var groups = groupsRepository.listAll();
        return addPerson.data("person", person)
                .data("tags", tags)
                .data("groups", groups)
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
                               @FormParam("defaultTag") String defaultTag,
                               @FormParam("groups") List<UUID> groupsIds) {
        Person person = Person.findById(id);
        if (person == null) {
            throw new WebApplicationException("Person not found", 404);
        }
        Tag tag = Tag.find("name", defaultTag).firstResult();
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;
        person.defaultTag = tag;

        addGroupsToPerson(groupsIds, person);

        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

    private static void addGroupsToPerson(List<UUID> groupsIds, Person person) {
        person.groups = new HashSet<>();
        if (groupsIds != null && !groupsIds.isEmpty()) {
            List<Group> groups = Group.find("id in ?1", groupsIds).list();
            person.groups.addAll(groups);

            // Dodatkowo dodaj osobę do grup po drugiej stronie relacji
            for (Group g : groups) {
                if (g.members == null) {
                    g.members = new HashSet<>();
                }
                g.members.add(person);
            }
        }
    }

}
