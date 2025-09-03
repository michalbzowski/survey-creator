package pl.bzowski.persons;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@RequestScoped
public class PersonRepository extends RepositoryBase {

    public PersonRepository() {
        //
    }

    public List<Person> listAll() {
        return Person.list("registeredUser.username", currentUsername());
    }

    public List<Person> listAll(Sort lastName) {
        return Person.list("registeredUser.username", lastName, currentUsername());
    }

    public void persist(Person person) {
        person.registeredUser = currentRegisteredUser();
        person.persist();
    }
}
