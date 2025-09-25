package pl.bzowski.groups;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;

import java.util.List;
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
    @WithTransaction
    public Uni<TemplateInstance> list() {
        return Group.listAll()
                .flatMap(groups -> Uni.createFrom()
                        .item(listGroups.data("groups", groups)));
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> createForm() {
        return personRepository.listAll()
                .flatMap(persons -> Uni.createFrom()
                        .item(createGroup.data("group", new Group(),
                                "persons", persons)));

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> create(@BeanParam GroupCreateRequest request) {
        return Uni.createFrom().item(Response.ok().build());
//        return personRepository.currentUserId()
//                .flatMap(uuid -> {
//                    // Tworzymy nową grupę
//                    Group group = new Group();
//                    group.name = request.name;
//                    group.registeredUserId = uuid;
//                    group.persist();
//
//                    // Pobieramy osoby po ID z requestu i przypisujemy do grupy
//                    if (request.persons != null && !request.persons.isEmpty()) {
//                        return Person
//                                .find("id in ?1", request.persons)
//                                .list()
//                                .flatMap(
//                                        selectedPersons -> {
//                                            selectedPersons.forEach(pp -> {
//                                                Person p = (Person) pp;
//                                                if (p.groups == null) {
//                                                    p.groups = new HashSet<>();
//                                                }
//                                                p.groups.add(group);
//                                                p.persist();
//                                            });
//                                            return Uni.createFrom().item(Response.status(Response.Status.SEE_OTHER)
//                                                    .location(java.net.URI.create("/web/groups"))
//                                                    .build());
//                                        }
//                                );
//                    }
//                    return null;
//                });
    }

    @GET
    @Path("/edit/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> editGroupForm(@PathParam("id") UUID id) {
        logger.info("Open Edit Group Form");

        return Group.findById(id)
                .onItem().ifNull().failWith(() -> new WebApplicationException("Grupa nie znaleziona", 404))
                .flatMap(group -> personRepository.listAll()
                        .map(persons ->
                                createGroup
                                        .data("group", group)
                                        .data("persons", persons)
                                        .data("edit", true)
                        ));
    }


    @POST
    @Path("/edit/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> editGroup(@PathParam("id") UUID id, @BeanParam GroupCreateRequest request) {
        logger.info("Group to edit: " + id.toString());

        return Group.findById(id)
                .flatMap(g -> {
                    Group group = (Group) g;
                    if (group == null) {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
                    }
                    group.name = request.name;

                    Uni<Void> updatePersonsUni;
                    if (group.members != null && !group.members.isEmpty()) {
                        // Usuń powiązania grupowe po stronie osób oraz z grupy
                        List<Uni<Void>> removeGroupFromPersons = group.members.stream().map(p -> {
                            if (p.groups != null) {
                                p.groups.remove(group);
                                return p.persistAndFlush().replaceWithVoid();
                            }
                            return Uni.createFrom().voidItem();
                        }).toList();

                        group.members.clear();
                        logger.info("Members cleared");
                        updatePersonsUni = Uni.combine().all().unis(removeGroupFromPersons).discardItems();
                    } else {
                        updatePersonsUni = Uni.createFrom().voidItem();
                    }

                    // Przypisz nowe osoby do grupy
                    Uni<List<Person>> selectedPersonsUni = (request.persons != null && !request.persons.isEmpty())
                            ? Person.find("id in ?1", request.persons).list()
                            : Uni.createFrom().item(List.of());

                    return updatePersonsUni
                            .flatMap(v -> selectedPersonsUni)
                            .flatMap(selectedPersons -> {
                                List<Uni<Void>> addGroupToPersons = selectedPersons.stream().map(p -> {
                                    if (p.groups == null) {
                                        p.groups = new java.util.HashSet<>();
                                    }
                                    p.groups.add(group);
                                    return p.persistAndFlush().replaceWithVoid();
                                }).toList();

                                group.members.addAll(selectedPersons);

                                return Uni.combine().all().unis(addGroupToPersons).discardItems()
                                        .flatMap(x -> group.persistAndFlush().replaceWithVoid());
                            })
                            .replaceWith(Response.status(Response.Status.SEE_OTHER)
                                    .location(java.net.URI.create("/web/groups"))
                                    .build());
                });
    }


    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @WithTransaction
    public Uni<Response> deleteGroup(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, Group::findById, "/web/groups");
    }
}
