package pl.bzowski.group;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@Singleton
public class GroupsRepository extends RepositoryBase {
    public Uni<List<Group>> listAll() {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(uuid -> Group.list("registeredUserId = ?1", Sort.by("name"), uuid));
    }
}
