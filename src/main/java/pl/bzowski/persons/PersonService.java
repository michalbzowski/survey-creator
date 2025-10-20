package pl.bzowski.persons;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import pl.bzowski.groups.Group;
import pl.bzowski.tags.Tag;

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
        return Person.<Person>findById(id)
                .onItem()
                .call(person -> Tag.find("registeredUserId = ?1 and name = ?2", person.registeredUserId, defaultTag).firstResult()
                        .onItem()
                        .call(tag -> {
                            person.firstName = firstName;
                            person.lastName = lastName;
                            person.email = email;
                            person.defaultTag = (Tag) tag;
                            addGroupsToPerson(groupsIds, person);
                            return Uni.createFrom().item(person);
                        })).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Boolean> deletePerson(UUID id) {
        return Person.deleteById(id);
    }

}