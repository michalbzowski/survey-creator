package pl.bzowski.attendances.entry;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.EmailAttendanceEntryLinkSentEvent;

@Singleton
public class CostamService {

    @WithTransaction
    public Uni<Void> persiste(EmailAttendanceEntryLinkSentEvent body) {
        CommunicationAttendanceLink c = new CommunicationAttendanceLink(body.communicationId(), body.attendanceEntryId());
        return Panache.getSession().flatMap(session -> session.persist(c));
    }
}
