package pl.bzowski.persons;

import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import pl.bzowski.group.Group;
import pl.bzowski.tags.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Singleton
@Transactional
public class PersonService {

    private final PersonRepository personRepository;

    @Inject
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person persist(String firstName, String lastName, String email, String defaultTag, List<UUID> groupsIds) {
        Tag tag = Tag.find("name", defaultTag).firstResult();
        Person person = new Person(firstName, lastName, email, tag);
        addGroupsToPerson(groupsIds, person);
        personRepository.persist(person);
        return person;
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

    public List<Person> listAll(Sort lastName) {
        return personRepository.listAll(lastName);
    }

    public void editPerson(UUID id, String firstName, String lastName, String email, String defaultTag, List<UUID> groupsIds) {
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
    }

    public void deletePerson(UUID id, String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Person person = Person.findById(id);
            if (person != null) {
                person.delete();
            }
        }
    }
}
