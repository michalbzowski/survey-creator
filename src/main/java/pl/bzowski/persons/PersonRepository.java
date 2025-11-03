package pl.bzowski.persons;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.shared.base.RepositoryBase;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class PersonRepository extends RepositoryBase {

    private static final Logger log = LoggerFactory.getLogger(PersonRepository.class);

    public PersonRepository() {
        //
    }

    public Uni<List<Person>> listAll() {
        return currentRegisteredUserId()
                .flatMap(uuid -> Person.list("registeredUserId", uuid));
    }

    public Uni<List<PersonBasicInfoDto>> listAllToBasicInfo() {
        return currentRegisteredUserId()
                .flatMap(uuid -> {
                    PanacheQuery<PersonBasicInfoDto> boardQuery =
                            Person.find("registeredUserId", uuid)
                                    .project(PersonBasicInfoDto.class);
                    return boardQuery.list();
                });
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

    public Uni<UUID> registeredUserId() {
        return currentRegisteredUserId();
    }

    public Uni<Boolean> hasCurrentUserAnyPerson() {
        log.info("method: hasCurrentUserAnyPerson");
        return registeredUserId()
                .onItem().invoke(uuid -> log.info("- registeredUserId: {}", uuid))
                .onItem()
                .transformToUni(uuid -> Person.count("registeredUserId = ?1", uuid))
                .onItem().invoke(count -> log.info("- registeredUserId count: {}", count))
                .onItem()
                .transform(count -> count > 0)
                .onItem().invoke(result -> log.info("- result: {}", result));
    }
}
