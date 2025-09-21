package pl.bzowski.communication;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logmanager.Level;
import pl.bzowski.email.EmailService;

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
    public void send(Communication communication) {
        String body = confirmation.data("firstName", communication.getPersonFirstName())
                .data("lastName", communication.getPersonLastName())
                .data("userEmail", communication.getProperty("userEmail"))
                .data("confirmationLink", communication.getProperty("confirmationLink"))
                .render();
        emailService.sendEmail(communication.getPersonEmail(), "Potwierdź adres email", body)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribe().with(
                        v -> {logger.info("Confirmation mail sent");
                            communication.statusSent();
                            communication.persist();},
                        f -> logger.log(Level.ERROR, "Confirmation failed", f)
                );
    }
}
