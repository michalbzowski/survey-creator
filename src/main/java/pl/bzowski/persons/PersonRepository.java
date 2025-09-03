package pl.bzowski.persons;

import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import pl.bzowski.security.RegisteredUser;

import java.util.List;

@RequestScoped
public class PersonRepository {

    @Inject
    SecurityIdentity securityIdentity;

    public List<Person> listAll() {
        var username = securityIdentity.getPrincipal().getName();
        return Person.list("registeredUser.username", username);
    }

    public List<Person> listAll(Sort lastName) {
        var username = securityIdentity.getPrincipal().getName();
        return Person.list("registeredUser.username", lastName, username);
    }

    public void persist(Person person) {
        String username = securityIdentity.getPrincipal().getName();
        person.registeredUser = RegisteredUser.find("username", username).firstResult();
        person.persist();
    }
}
