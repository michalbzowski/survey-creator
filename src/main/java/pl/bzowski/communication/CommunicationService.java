package pl.bzowski.communication;

import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Singleton;
import org.jboss.logmanager.Level;

import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class CommunicationService {

    private final Logger logger = Logger.getLogger(CommunicationService.class.getName());

    private final CommunicationRepository communicationRepository;
    private final CommunicationSenderFactory communicationSenderFactory;
    private final Vertx vertx;

    public CommunicationService(CommunicationRepository communicationRepository, CommunicationSenderFactory communicationSenderFactory, Vertx vertx) {
        this.communicationRepository = communicationRepository;
        this.communicationSenderFactory = communicationSenderFactory;
        this.vertx = vertx;
    }


    public Uni<UUID> persistCommunication(CommunicationDto body) {
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

    public Uni<Void> send(UUID id) {
        return Communication.<Communication>findById(id)
                .flatMap(communication -> {
                    logger.info("Success");
                    CommunicationSender communicationSender = communicationSenderFactory.create(communication.getCommunicationTemplate());
                    communicationSender.send(communication);
                    return Uni.createFrom().voidItem();
                });
    }
}
