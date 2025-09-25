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
public class EmailAttendanceRecordLinkSender implements CommunicationSender {

    public static final String EMAIL_ATTENDANCE_ENTRY_LINK_SENT = "EMAIL_ATTENDANCE_RECORD_LINK_SENT";
    private final Logger logger = Logger.getLogger(EmailNewPersonAddedCommunicationSender.class.getName());

    @Inject
    EmailService emailService;

    @Inject
    EventBus eventBus;

    @Location("email/attendanceRecordLink")
    Template attendanceRecordLink;

    @Override
    public Uni<Void> send(Communication communication) {
        String render = attendanceRecordLink.data("eventTitle", communication.getProperty("eventTitle"))
                .data("appHost", communication.getProperty("appHost"))
                .data("attendanceRecordLink", communication.getProperty("attendanceRecordLink"))
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
                    var attendanceEntryId = UUID.fromString(communication.getProperty("attendanceEntryId").toString());
                    eventBus.publish(EMAIL_ATTENDANCE_ENTRY_LINK_SENT, new EmailAttendanceEntryLinkSentEvent(communicationId, attendanceEntryId));
                })
                .onFailure().invoke(t ->
                        logger.log(Level.FINEST, "Confirmation failed", t));
    }

}
