package pl.bzowski.group;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.links.LinkGenerationResource;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;
import pl.bzowski.tags.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/web/groups")
public class GroupsPageResource {

    Logger logger = Logger.getLogger(GroupsPageResource.class.getName());

    private final Template createGroup;

    private final Template listGroups;

    private final PersonRepository personRepository;

    @Inject
    public GroupsPageResource(Template createGroup, Template listGroups, PersonRepository personRepository) {

        this.createGroup = createGroup;
        this.listGroups = listGroups;
        this.personRepository = personRepository;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        List<Group> groups = Group.listAll();
        return listGroups.data("groups", groups);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        // Pobierz listę osób do wyboru w formularzu
        List<Person> persons = personRepository.listAll();
        Group group = new Group();
        return createGroup.data("group", group,
                "persons", persons);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response create(@BeanParam GroupCreateRequest request) {

        // Tworzymy nową grupę
        Group group = new Group();
        group.name = request.name;
        group.registeredUserId = personRepository.currentUserId();
        group.persist();

        // Pobieramy osoby po ID z requestu i przypisujemy do grupy
        if (request.persons != null && !request.persons.isEmpty()) {
            List<Person> selectedPersons = Person.find("id in ?1", request.persons).list();
            for (Person p : selectedPersons) {
                if (p.groups == null) {
                    p.groups = new java.util.HashSet<>();
                }
                p.groups.add(group);
                p.persist();
            }
        }

        // Po utworzeniu grupy redirect do listy lub strony potwierdzenia
        return Response.status(Response.Status.SEE_OTHER)
                .location(java.net.URI.create("/web/groups"))
                .build();
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editGroupForm(@PathParam("id") UUID id) {
        logger.info("Open Edit Group Form");
        Group group = Group.findById(id);
        if (group == null) {
            throw new WebApplicationException("Grupa nie znaleziona", 404);
        }
        logger.info("Persons in group: " + group.members.size());
        List<Person> persons = personRepository.listAll();
        return createGroup
                .data("group", group)
                .data("persons", persons)
                .data("edit", true);
    }

    @POST
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response editGroup(@PathParam("id") UUID id, @BeanParam GroupCreateRequest request) {
        logger.info("Group to edit: " + id.toString());
        Group group = Group.findById(id);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        group.name = request.name;

        // Usuń powiązania grupowe po stronie osób
        if (group.members != null) {
            for (Person p : group.members) {
                if (p.groups != null) {
                    p.groups.remove(group);
                    p.persist();
                }
            }

            // Obsługa przypisania osób do grupy
            // Najpierw usuń stare powiązania
            group.members.clear();
            logger.info("Members cleared");
        }
        // Dodaj nowe powiązania
        if (request.persons != null && !request.persons.isEmpty()) {
            logger.info("Persons: " + request.persons.size());
            List<Person> selectedPersons = Person.find("id in ?1", request.persons).list();
            for (Person p : selectedPersons) {
                // Dodaj grupę po stronie osoby
                if (p.groups == null) {
                    p.groups = new java.util.HashSet<>();
                }
                p.groups.add(group);
                p.persist();
            }
            // Dodaj osoby do grupy
            group.members.addAll(selectedPersons);
        }
        group.persist();

        return Response.status(Response.Status.SEE_OTHER)
                .location(java.net.URI.create("/web/groups"))
                .build();
    }


    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deleteGroup(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Group group = Group.findById(id);
            if (group != null) {
                group.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/web/groups").build()).build();
    }
}
