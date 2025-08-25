package pl.bzowski.email;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.mailer.MockMailbox;

import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EmailServiceTest {

    private static final String RECIPIENT = "test@example.com";

    @Inject
    EmailService emailService;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    public void clearMailbox() {
        mailbox.clear();
    }

    @Test
    public void testSendEmail() {
        String subject = "Test subject";
        String body = "Test email body";

        emailService.sendEmail(RECIPIENT, subject, body);

        // Sprawdzenie, czy wiadomość została wysłana
        assertThat(mailbox.getTotalMessagesSent()).isEqualTo(1);

        MailMessage sentMail = mailbox.getMailMessagesSentTo(RECIPIENT).get(0);

        assertThat(sentMail.getSubject()).isEqualTo(subject);
        assertThat(sentMail.getText()).contains(body);
    }
}