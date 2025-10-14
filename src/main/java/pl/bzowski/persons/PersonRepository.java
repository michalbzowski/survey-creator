package pl.bzowski.persons;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.shared.base.RepositoryBase;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class PersonRepository extends RepositoryBase {

    public PersonRepository() {
        //
    }

    public Uni<List<Person>> listAll() {
        return currentRegisteredUserId()
                .flatMap(uuid -> Person.list("registeredUserId", uuid));
    }

    public Uni<List<Person>> listAll(Sort lastName) {
        return currentRegisteredUserId()
                .flatMap(uuid -> Person.list("registeredUserId", lastName, uuid));
    }

    public Uni<Person> persist(Person person) {
        return currentRegisteredUserId()
                .flatMap(u -> {
                    person.registeredUserId = u;
                    return person.persist();
                });
    }

    public Uni<UUID> currentUserId() {
        return currentRegisteredUserId();
    }
}
