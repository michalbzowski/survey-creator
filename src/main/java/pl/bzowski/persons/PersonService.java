package pl.bzowski.persons;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import pl.bzowski.group.Group;
import pl.bzowski.group.GroupsPageResource;
import pl.bzowski.tags.Tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Singleton
public class PersonService {

    private final PersonRepository personRepository;

    @Inject
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Uni<Person> persist(String firstName, String lastName, String email, String defaultTag, List<UUID> groupsIds) {
        return Tag.find("name", defaultTag).firstResult().flatMap(tag -> {
            Person person = new Person(firstName, lastName, email, (Tag) tag);
            addGroupsToPerson(groupsIds, person);
            return personRepository.persist(person);
        });
    }

    private static void addGroupsToPerson(List<UUID> groupsIds, Person person) {
        person.groups = new HashSet<>();
        if (groupsIds != null && !groupsIds.isEmpty()) {
            Group.find("id in ?1", groupsIds).<Group>list()
                    .flatMap(groups -> {
                        person.groups.addAll(groups);
                        // Dodatkowo dodaj osobę do grup po drugiej stronie relacji
                        for (Group g : groups) {
                            if (g.members == null) {
                                g.members = new HashSet<>();
                            }
                            g.members.add(person);
                        }
                        return Uni.createFrom().voidItem();
                    });

        }
    }

    public Uni<List<Person>> listAll(Sort lastName) {
        return personRepository.listAll(lastName);
    }

    public Uni<Void> editPerson(UUID id, String firstName, String lastName, String email, String defaultTag, List<UUID> groupsIds) {
        Uni<Person> p = Person.findById(id);
        Uni<Tag> t = Tag.find("name", defaultTag).firstResult();
        return Uni.combine()
                .all()
                .unis(p, t)
                .asTuple()
                .onItem()
                .transformToUni(tuple -> {
                    Person person = tuple.getItem1();
                    Tag tag = tuple.getItem2();
                    if (person == null) {
                        throw new WebApplicationException("Person not found", 404);
                    }
                    person.firstName = firstName;
                    person.lastName = lastName;
                    person.email = email;
                    person.defaultTag = tag;

                    addGroupsToPerson(groupsIds, person);
                    return Uni.createFrom().voidItem();
                });

    }

    @WithTransaction
    public Uni<Void> deletePerson(UUID id, String method) {
        if (!"delete".equalsIgnoreCase(method)) {
            return Uni.createFrom().voidItem();
        }
        return Person.findById(id)
                .flatMap(person -> {
                    if (person == null) {
                        return Uni.createFrom().voidItem();
                    }
                    return person.delete();
                });
    }

}