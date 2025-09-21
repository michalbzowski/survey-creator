package pl.bzowski.communication;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import pl.bzowski.base.RepositoryBase;
import io.vertx.core.Vertx;

import java.util.UUID;

@Singleton
public class CommunicationRepository extends RepositoryBase {

    @Inject
    Vertx vertx;

    @Transactional
    public UUID persistBlocking(Communication communication) {
        communication.persist();
        return communication.getId();
    }

    public Uni<UUID> persist(Communication communication) {
        return Uni.createFrom().completionStage(
                vertx.executeBlocking(() -> persistBlocking(communication))
                        .toCompletionStage()
        );
    }
}
