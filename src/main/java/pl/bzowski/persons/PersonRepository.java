package pl.bzowski.persons;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class PersonRepository extends RepositoryBase {

    public PersonRepository() {
        //
    }

    public List<Person> listAll() {
        return Person.list("registeredUserId", currentRegisteredUserId());
    }

    public List<Person> listAll(Sort lastName) {
        return Person.list("registeredUserId", lastName, currentRegisteredUserId());
    }

    public void persist(Person person) {
        person.registeredUserId = currentRegisteredUserId();
        person.persist();
    }

    public UUID currentUserId() {
        return currentRegisteredUserId();
    }
}
