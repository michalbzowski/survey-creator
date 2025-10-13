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
public class MemberAssignedMailSender implements CommunicationSender {

    public static final String MEMBER_ASSIGNED_MAIL_SENT = "MEMBER_ASSIGNED_MAIL_SENT";
    private final Logger logger = Logger.getLogger(MemberAssignedMailSender.class.getName());

    @Inject
    EmailService emailService;

    @Inject
    EventBus eventBus;

    @Location("email/memberPresenceConfirmation")
    Template memberPresenceConfirmation;

    @Override
    public Uni<Void> send(Communication communication) {
        String render = memberPresenceConfirmation
                .data("eventTitle", communication.getProperty("eventTitle"))
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
                    eventBus.publish(MEMBER_ASSIGNED_MAIL_SENT, new MemberAssignedMailSentEvent(communicationId, teamEntryId));
                })
                .onFailure().invoke(t ->
                        logger.log(Level.FINEST, "Confirmation failed", t));
    }

}
