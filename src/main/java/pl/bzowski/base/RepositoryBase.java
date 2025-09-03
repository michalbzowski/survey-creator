package pl.bzowski.base;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import pl.bzowski.security.RegisteredUser;


public class RepositoryBase {

    @Inject
    SecurityIdentity securityIdentity;

    protected String currentUsername() {
        return securityIdentity.getPrincipal().getName();
    }

    protected RegisteredUser currentRegisteredUser() {
        return RegisteredUser.find("username", currentUsername()).firstResult();
    }

}
