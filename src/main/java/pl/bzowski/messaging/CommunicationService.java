package pl.bzowski.messaging;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.jboss.logmanager.Level;

import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class CommunicationService {

    private final Logger logger = Logger.getLogger(CommunicationService.class.getName());

    private final CommunicationRepository communicationRepository;
    private final CommunicationSenderFactory communicationSenderFactory;

    public CommunicationService(CommunicationRepository communicationRepository, CommunicationSenderFactory communicationSenderFactory) {
        this.communicationRepository = communicationRepository;
        this.communicationSenderFactory = communicationSenderFactory;
    }


    public Uni<UUID> persistCommunication(PersistCommunicationCommand body) {
        logger.log(Level.DEBUG, "persistCommunication: " + body.toString());
        Communication communication = new Communication(
                body.getChannel(),
                body.getCommunicationTemplate(),
                body.getPersonId(),
                body.getPersonFirstName(),
                body.getPersonLastName(),
                body.getPersonEmail(),
                body.getStatus(),
                body.getProperties()
        );
        return communicationRepository.persist(communication)
                .onItem()
                .invoke(id -> logger.info("Communication persisted with id: " + id));
    }

    @WithTransaction
    public Uni<Void> send(UUID id) {
        return Communication.<Communication>findById(id)
                .flatMap(communication -> {
                    logger.info("Success");
                    CommunicationSender communicationSender = communicationSenderFactory.create(communication.getCommunicationTemplate());
                    // Tutaj zwracamy Uni przesłane z wywołania send, łącząc je z łańcuchem
                    return communicationSender.send(communication);
                });
    }
}
