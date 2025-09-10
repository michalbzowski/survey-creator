package pl.bzowski.base;

import io.quarkus.oidc.OidcProviderClient;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusIdentityProviderManagerImpl;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;
import java.util.logging.Logger;


public class RepositoryBase {

    private static final Logger logger = Logger.getLogger(RepositoryBase.class.getName());

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    protected String currentUsername() {
        var username = securityIdentity.getPrincipal().getName();
        logger.info("currentUsername: " + username);
        return username;
    }

    protected UUID currentRegisteredUserId() {
        String sub = jwt.getClaim("sub").toString();
        logger.info("sub: " + sub);
        return UUID.fromString(sub);
    }

}
