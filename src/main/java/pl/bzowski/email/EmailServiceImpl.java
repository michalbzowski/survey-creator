package pl.bzowski.email;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;

import static pl.bzowski.configurations.Configurations.EMAIL_FROM;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    @Inject
    ReactiveMailer reactiveMailer;

    @Inject
    ConfigurationsRepository configurationsRepository;

    @ConfigProperty(name = "quarkus.mailer.username")
    String username;

    @Override
    public Uni<Void> sendEmail(String to, String subject, String body) {
        Mail mail = Mail.withHtml(to, subject, body);
        String emailFrom = (String) configurationsRepository.getConfigurations().get(EMAIL_FROM);
        if (emailFrom != null && !emailFrom.isEmpty()) {
            mail.setFrom(String.format("\"%s\" <%s>", emailFrom, username));
        } else {
            mail.setFrom(username);
        }
        return reactiveMailer.send(mail);
    }
}
