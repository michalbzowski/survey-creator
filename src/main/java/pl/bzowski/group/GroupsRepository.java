package pl.bzowski.group;

import io.quarkus.panache.common.Sort;
import jakarta.inject.Singleton;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@Singleton
public class GroupsRepository extends RepositoryBase {
    public List<Group> listAll() {
        return Group.list("registeredUserId = ?1", Sort.by("name"), currentRegisteredUserId());
    }
}
