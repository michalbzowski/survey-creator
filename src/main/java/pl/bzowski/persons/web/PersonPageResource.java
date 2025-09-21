package pl.bzowski.persons.web;

import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.Level;
import pl.bzowski.base.CurrentUserRepository;
import pl.bzowski.communication.*;
import pl.bzowski.group.Group;
import pl.bzowski.group.GroupsRepository;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.persons.PersonService;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static pl.bzowski.communication.CommunicationEventListener.PERSIST_COMMUNICATION;

@Path("/web/persons")
public class PersonPageResource {

    @ConfigProperty(name = "app.host")
    String appHost;

    Logger logger = Logger.getLogger(PersonPageResource.class.getName());

    private final Template addPerson;
    private final Template listPersons;
    private final TagsRepository tagsRepository;
    private final PersonService personService;
    private final GroupsRepository groupsRepository;
    private final CurrentUserRepository currentUserRepository;
    private final EventBus eventBus;

    public PersonPageResource(Template addPerson, Template listPersons, TagsRepository tagsRepository, PersonService personService, GroupsRepository groupsRepository, CurrentUserRepository currentUserRepository, EventBus eventBus) {
        this.addPerson = addPerson;
        this.listPersons = listPersons;
        this.tagsRepository = tagsRepository;
        this.personService = personService;
        this.groupsRepository = groupsRepository;
        this.currentUserRepository = currentUserRepository;
        this.eventBus = eventBus;
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
        Person person;
        CommunicationPersonAgreement cpa;
        try {
            person = personService.persist(firstName, lastName, email, defaultTag, groupsIds);
            cpa = new CommunicationPersonAgreement();
            cpa.channel = Channel.EMAIL;
            cpa.personId = person.id;
            cpa.personEmail = email;
            cpa.registeredUserId = currentUserRepository.currentRegisteredUserId();
            cpa.persist();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.toString());
            return Response.serverError().build();
        }
        logger.info("start \"save-communication\"");
        eventBus.publish(PERSIST_COMMUNICATION,
                new CommunicationDto(
                        Channel.EMAIL,
                        CommunicationTemplate.EMAIL_NEW_PERSON_ADDED,
                        person,
                        Map.of(
                                "userEmail", currentUserRepository.currentRegisteredUserEmail(),
                                "confirmationLink", appHost + "/web/communication/confirm/" + cpa.id
                        )));
        logger.info("finished \"save-communication\"");
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listPersons() {
        List<Person> persons = personService.listAll(Sort.by("lastName"));
        return listPersons.data("persons", persons);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        personService.deletePerson(id, method);
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
    public Response editPerson(@PathParam("id") UUID id,
                               @FormParam("firstName") String firstName,
                               @FormParam("lastName") String lastName,
                               @FormParam("email") String email,
                               @FormParam("defaultTag") String defaultTag,
                               @FormParam("groups") List<UUID> groupsIds) {
        personService.editPerson(id, firstName, lastName, email, defaultTag, groupsIds);
        return Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build();
    }
}
