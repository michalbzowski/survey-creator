package pl.bzowski.email;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    @Inject
    Mailer mailer;

    @Override
    public void sendEmail(String to, String subject, String body) {
        mailer.send(Mail.withText(to, subject, body));
    }
}
