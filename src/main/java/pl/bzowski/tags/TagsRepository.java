package pl.bzowski.tags;

import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@RequestScoped
public class TagsRepository extends RepositoryBase {

    public List<Tag> listAll() {
        return Tag.list("registeredUserId", currentRegisteredUserId());
    }

    public void createTag(String name) {
        Tag tag = new Tag(name, currentRegisteredUserId());
        tag.persist();
    }
}
