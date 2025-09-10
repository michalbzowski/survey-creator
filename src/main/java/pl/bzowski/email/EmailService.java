package pl.bzowski.email;


import io.smallrye.mutiny.Uni;

public interface EmailService {
    Uni<Void> sendEmail(String to, String subject, String body);
}

