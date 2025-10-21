package pl.bzowski.tags;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.persons.Person;
import pl.bzowski.shared.base.RepositoryBase;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class TagsRepository extends RepositoryBase {

    @WithTransaction
    public Uni<List<Tag>> listAll() {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(registeredUserId -> Tag.list("registeredUserId", registeredUserId)
                );
    }

    @WithTransaction
    public Uni<Tag> createTag(String name) {
        return currentRegisteredUserId()
                .onItem().transformToUni(userId -> {
                    Tag tag = new Tag(name, userId);
                    return tag.persist();
                });
    }

    public Uni<Set<String>> deleteTag(UUID id) {
        return Person.<Person>find("defaultTag.id = ?1", id)
                .list()
                .onItem()
                .transformToUni(p -> Uni.createFrom().item(p.stream()
                        .map(pp -> pp.email)
                        .collect(Collectors.toSet())))
                .onItem()
                .transformToUni(set -> {
                    if (set.isEmpty()) {
                        return Tag.deleteById(id).flatMap(r -> Uni.createFrom().item(set));
                    } else {
                        return Uni.createFrom().item(set);
                    }
                });
    }
}
