package pl.bzowski.tags;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import pl.bzowski.security.RegisteredUser;

import java.util.List;

@RequestScoped
public class TagsRepository {
    @Inject
    SecurityIdentity securityIdentity;

    public List<Tag> listAll() {
        var username = securityIdentity.getPrincipal().getName();
        return Tag.list("registeredUser.username", username);
    }

    public void createTag(String name) {
        String username = securityIdentity.getPrincipal().getName();
        RegisteredUser registeredUser = RegisteredUser.find("username", username).firstResult();
        Tag tag = new Tag(name, registeredUser);
        tag.persist();
    }
}
