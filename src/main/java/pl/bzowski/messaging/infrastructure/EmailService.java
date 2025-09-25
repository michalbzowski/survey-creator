package pl.bzowski.messaging.infrastructure;


import io.smallrye.mutiny.Uni;

public interface EmailService {
    Uni<Void> sendEmail(String to, String subject, String body);
}

