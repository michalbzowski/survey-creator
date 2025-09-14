package pl.bzowski.email;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.MailMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    @Inject
    io.vertx.mutiny.ext.mail.MailClient mailClient;

    @Override
    public Uni<Void> sendEmail(String to, String subject, String body) {

        MailMessage message = new MailMessage();
        message.setFrom("potwierdzobecnosc.pl <noreplay@potwierdzobecnosc.pl>");
        message.setTo(to);
        message.setSubject(subject);
        message.setHtml(body);
        return mailClient.sendMail(message).replaceWithVoid();
    }
}
