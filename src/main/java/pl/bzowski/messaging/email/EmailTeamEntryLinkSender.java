package pl.bzowski.messaging.email;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pl.bzowski.messaging.Communication;
import pl.bzowski.messaging.CommunicationSender;
import pl.bzowski.messaging.infrastructure.EmailService;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class EmailTeamEntryLinkSender implements CommunicationSender {

    public static final String EMAIL_TEAM_ENTRY_LINK_SENT = "EMAIL_TEAM_RECORD_LINK_SENT";
    private final Logger logger = Logger.getLogger(EmailNewPersonAddedCommunicationSender.class.getName());

    @Inject
    EmailService emailService;

    @Inject
    EventBus eventBus;

    @Location("email/teamEntryLink")
    Template teamEntryLink;

    @Override
    public Uni<Void> send(Communication communication) {
        String render = teamEntryLink.data("eventTitle", communication.getProperty("eventTitle"))
                .data("appHost", communication.getProperty("appHost"))
                .data("teamEntryLink", communication.getProperty("teamEntryLink"))
                .data("personEmail", communication.getProperty("personEmail"))
                .render();

        return emailService.sendEmail(communication.getPersonEmail(), "Czy będziesz obecny?", render)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(_ -> {
                    logger.info("Confirmation mail sent");
                    communication.statusSent();
                    return communication.persist().replaceWithVoid();
                })
                .onItem().invoke(() -> {
                    logger.info("Communication status updated and persisted");
                    var communicationId = communication.getId();
                    var teamEntryId = UUID.fromString(communication.getProperty("teamEntryId").toString());
                    eventBus.publish(EMAIL_TEAM_ENTRY_LINK_SENT, new EmailTeamEntryLinkSentEvent(communicationId, teamEntryId));
                })
                .onFailure().invoke(t ->
                        logger.log(Level.FINEST, "Confirmation failed", t));
    }

}
