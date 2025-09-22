package pl.bzowski.tags;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@RequestScoped
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
}
