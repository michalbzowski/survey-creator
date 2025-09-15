package pl.bzowski.email;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    @Inject
    ReactiveMailer reactiveMailer;

    @Override
    public Uni<Void> sendEmail(String to, String subject, String body) {
        return reactiveMailer.send(Mail.withHtml(to, subject, body));
    }
}
