package pl.bzowski.communication;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.base.RepositoryBase;

import java.util.UUID;

@Singleton
public class CommunicationRepository extends RepositoryBase {

    @WithTransaction
    public Uni<UUID> persist(Communication communication) {
        return communication.<Communication>persist()
                .onItem()
                .transformToUni(c -> Uni.createFrom().item(c.getId()));
    }

}
