package pl.bzowski.persons.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.Level;
import pl.bzowski.shared.base.CurrentUserRepository;
import pl.bzowski.messaging.*;
import pl.bzowski.messaging.agreement.CommunicationAgreement;
import pl.bzowski.groups.Group;
import pl.bzowski.groups.GroupsRepository;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonService;
import pl.bzowski.tags.Tag;
import pl.bzowski.tags.TagsRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static pl.bzowski.messaging.CommunicationEventListener.PERSIST_COMMUNICATION;

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
    @WithTransaction
    public Uni<TemplateInstance> showAddForm() {
        return tagsRepository.listAll()
                .flatMap(
                        tags -> groupsRepository
                                .listAll()
                                .flatMap(groups -> Uni.createFrom().item(addPerson.data(
                                        "person", new Person(),
                                        "tags", tags,
                                        "groups", groups)))
                );
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> addPerson(@FormParam("firstName") String firstName,
                                   @FormParam("lastName") String lastName,
                                   @FormParam("email") String email,
                                   @FormParam("defaultTag") String defaultTag,
                                   @FormParam("groups") List<UUID> groupsIds) {
        return personService.persist(firstName, lastName, email, defaultTag, groupsIds)
                .flatMap(person -> {
                    CommunicationAgreement cpa = new CommunicationAgreement();
                    cpa.channel = Channel.EMAIL;
                    cpa.personId = person.id;
                    cpa.personEmail = email;

                    return currentUserRepository.currentRegisteredUserId()
                            .flatMap(registeredUserId -> {
                                cpa.registeredUserId = registeredUserId;
                                return cpa.persistAndFlush();
                            })
                            .invoke(() -> {
                                logger.info("start \"save-communication\"");
                                eventBus.publish(PERSIST_COMMUNICATION,
                                        new PersistCommunicationCommand(Channel.EMAIL,
                                                CommunicationTemplate.EMAIL_NEW_PERSON_ADDED,
                                                cpa.registeredUserId,
                                                person,
                                                Map.of(
                                                        "userEmail", currentUserRepository.currentRegisteredUserEmail(),
                                                        "confirmationLink", appHost + "/web/communication/confirm/" + cpa.id
                                                )));
                                logger.info("finished \"save-communication\"");
                            })
                            .replaceWith(Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    logger.log(Level.ERROR, throwable.toString());
                    return Response.serverError().build();
                });
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    @WithTransaction
    public Uni<TemplateInstance> listPersons() {
        return personService.listAll(Sort.by("lastName"))
                .flatMap(persons -> Uni.createFrom().item(listPersons.data("persons", persons)));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Response> deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if (!"delete".equalsIgnoreCase(method)) {
            return Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build());
        } else {
            return personService.deletePerson(id)
                    .onItem()
                    .transform(t -> Response.ok().build());
        }
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> showEditForm(@PathParam("id") UUID id) {
        Uni<Person> personUni = Person.findById(id);
        Uni<List<Tag>> tagsUni = tagsRepository.listAll();
        Uni<List<Group>> groupsUni = groupsRepository.listAll();
        return personUni
                .flatMap(person -> tagsUni
                        .flatMap(tags -> groupsUni
                                .flatMap(groups -> Uni.createFrom().item(addPerson
                                        .data("person", person)
                                        .data("tags", tags)
                                        .data("groups", groups)
                                        .data("edit", true))

                                )));
    }


    @POST
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> editPerson(@PathParam("id") UUID id,
                                    @FormParam("firstName") String firstName,
                                    @FormParam("lastName") String lastName,
                                    @FormParam("email") String email,
                                    @FormParam("defaultTag") String defaultTag,
                                    @FormParam("groups") List<UUID> groupsIds) {
        return personService
                .editPerson(id, firstName, lastName, email, defaultTag, groupsIds)
                .onItem()
                .transformToUni(a -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/persons").build()).build()));
    }
}
