package pl.bzowski.shared.base;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;


public class RepositoryBase {

    private static final Logger logger = Logger.getLogger(RepositoryBase.class.getName());

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    ThreadContext threadContext;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    JsonWebToken jwt;

    protected String currentUsername() {
        var username = securityIdentity.getPrincipal().getName();
        logger.info("currentUsername: " + username);
        return username;
    }

    public Uni<UUID> currentRegisteredUserId() {
        return Uni.createFrom().item(this::getRegisteredUserId);
    }

    public UUID getRegisteredUserId() {
        String sub = jwt.getClaim("sub").toString();
        logger.info("sub: " + sub);
        return UUID.fromString(sub);
    }


    public CompletionStage<UUID> completionCurrentRegisteredUserId() {
        CompletableFuture<UUID> future = CompletableFuture.supplyAsync(this::getRegisteredUserId);
        return threadContext.withContextCapture(future)
                .thenApplyAsync(uuid -> uuid, managedExecutor);
    }

    public String currentRegisteredUserEmail() {
        logger.info(jwt.getClaimNames().toString());
        String firstName = jwt.getClaim("email").toString();
        logger.info("email: " + firstName);
        return firstName;
    }
}
