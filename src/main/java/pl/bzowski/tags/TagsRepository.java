package pl.bzowski.tags;

import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;

import java.util.List;

@RequestScoped
public class TagsRepository extends RepositoryBase {

    public List<Tag> listAll() {
        return Tag.list("registeredUser.username", currentUsername());
    }

    public void createTag(String name) {
        Tag tag = new Tag(name, currentRegisteredUser());
        tag.persist();
    }
}
