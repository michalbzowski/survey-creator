package pl.bzowski.messaging.email;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;
import pl.bzowski.messaging.Communication;
import pl.bzowski.messaging.CommunicationSender;
import pl.bzowski.messaging.infrastructure.EmailService;

import java.util.logging.Logger;

import static pl.bzowski.configurations.Configurations.EMAIL_FROM;

@Singleton
public class EmailNewPersonAddedCommunicationSender implements CommunicationSender {

    private final Logger logger = Logger.getLogger(EmailNewPersonAddedCommunicationSender.class.getName());

    @Inject
    EmailService emailService;

    @Inject
    @Location("email/confirmation")
    Template confirmation;

    @Inject
    ConfigurationsRepository configurationsRepository;

    @Override
    public Uni<Void> send(Communication communication) {
        String body = confirmation
                .data("firstName", communication.getPersonFirstName())
                .data("lastName", communication.getPersonLastName())
                .data("userEmail", communication.getProperty("userEmail"))
                .data("confirmationLink", communication.getProperty("confirmationLink"))
                .render();
        return configurationsRepository
                .getConfigurationsForUser(communication.getCurrentUserId())
                .onItem()
                .transform(configuration -> {
                    String emailFrom = (String) configuration.get(EMAIL_FROM);
                    return emailService.sendEmail(communication.getPersonEmail(), "Potwierdź adres email", body, emailFrom);
                }).replaceWithVoid();
    }
}
