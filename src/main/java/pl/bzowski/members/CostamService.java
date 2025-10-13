package pl.bzowski.members;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.MemberAssignedMailSentEvent;

@Singleton
public class CostamService {

    @WithTransaction
    public Uni<Void> persist(MemberAssignedMailSentEvent body) {
        CommunicationTeamLink c = new CommunicationTeamLink(body.communicationId(), body.teamEntryId());
        return Panache.getSession().flatMap(session -> session.persist(c));
    }
}
