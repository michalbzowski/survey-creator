package pl.bzowski.messaging.email;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pl.bzowski.messaging.Communication;
import pl.bzowski.messaging.CommunicationSender;
import pl.bzowski.messaging.infrastructure.EmailService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class EmailNewPersonAddedCommunicationSender implements CommunicationSender {

    private final Logger logger = Logger.getLogger(EmailNewPersonAddedCommunicationSender.class.getName());

    @Inject
    EmailService emailService;

    @Inject
    @Location("email/confirmation")
    Template confirmation;

    @Override
    public Uni<Void> send(Communication communication) {
        String body = confirmation
                .data("firstName", communication.getPersonFirstName())
                .data("lastName", communication.getPersonLastName())
                .data("userEmail", communication.getProperty("userEmail"))
                .data("confirmationLink", communication.getProperty("confirmationLink"))
                .render();
        return emailService.sendEmail(communication.getPersonEmail(), "Potwierdź adres email", body)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .flatMap(_ -> {
                    logger.info("Confirmation mail sent");
                    communication.statusSent();
                    return communication.persist().replaceWithVoid();
                })
                // Zwracamy Uni<Void>, finalizując operację
                .onItem().invoke(() ->
                        logger.info("Communication status updated and persisted"))
                // Poprawne reaktywne logowanie błędów
                .onFailure().invoke(t -> logger.log(Level.FINEST, "Confirmation failed", t));
    }
}
