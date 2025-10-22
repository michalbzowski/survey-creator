package pl.bzowski.messaging.infrastructure;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;


@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    @Inject
    ReactiveMailer reactiveMailer;

    @ConfigProperty(name = "quarkus.mailer.username")
    String username;

    @Override
    public Uni<Void> sendEmail(String to, String subject, String body, String emailFrom) {
        Mail mail = Mail.withHtml(to, subject, body);
        if (emailFrom != null && !emailFrom.isEmpty()) {
            mail.setFrom(String.format("%s <%s>", emailFrom, username));
        } else {
            mail.setFrom(username);
        }
        reactiveMailer.send(mail)
                .subscribe().with(
                        _ -> System.out.printf("Mail to %s subject %s sent", to, subject),
                        f -> System.out.printf("Mail to %s subject %s failed: %s", to, subject, f));
        return Uni.createFrom().voidItem();
    }
}
