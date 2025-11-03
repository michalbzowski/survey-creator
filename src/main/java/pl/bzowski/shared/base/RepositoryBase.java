package pl.bzowski.shared.base;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class RepositoryBase {

    private static final Logger log = LoggerFactory.getLogger(RepositoryBase.class);

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
        log.info("currentUsername: {}", username);
        return username;
    }

    public Uni<UUID> currentRegisteredUserId() {
        return Uni.createFrom().item(this::getRegisteredUserId);
    }

    public UUID getRegisteredUserId() {
        String sub = jwt.getClaim("sub").toString();
        log.info("sub: {}", sub);
        return UUID.fromString(sub);
    }


    public CompletionStage<UUID> completionCurrentRegisteredUserId() {
        CompletableFuture<UUID> future = CompletableFuture.supplyAsync(this::getRegisteredUserId);
        return threadContext.withContextCapture(future)
                .thenApplyAsync(uuid -> uuid, managedExecutor);
    }

    public String currentRegisteredUserEmail() {
        log.info(jwt.getClaimNames().toString());
        String firstName = jwt.getClaim("email").toString();
        log.info("email: {}", firstName);
        return firstName;
    }
}
