package pl.bzowski.messaging;

import io.smallrye.mutiny.Uni;

public interface CommunicationSender {
    Uni<Void> send(Communication communication);
}
