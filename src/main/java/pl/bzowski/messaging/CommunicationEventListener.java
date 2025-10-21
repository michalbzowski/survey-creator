package pl.bzowski.messaging;

import io.quarkus.vertx.ConsumeEvent;

import io.vertx.core.eventbus.Message;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logmanager.Level;

import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class CommunicationEventListener {

    public static final String PERSIST_COMMUNICATION = "persist-communication";
    public static final String COMMUNICATION_PERSISTED = "communication-persisted";

    private final Logger logger = Logger.getLogger(CommunicationEventListener.class.getName());

    private final CommunicationService communicationService;
    private final EventBus eventBus;

    public CommunicationEventListener(CommunicationService communicationService, EventBus eventBus) {
        this.communicationService = communicationService;
        this.eventBus = eventBus;
    }

    @ConsumeEvent(PERSIST_COMMUNICATION)
    public void process(Message<PersistCommunicationCommand> msg) {
        logger.info("Event message got on: " + msg.address());
        communicationService.persistCommunication(msg.body())
                .subscribe().with(
                        id -> {
                            logger.info("Persist succeeded: " + id);
                            eventBus.publish(COMMUNICATION_PERSISTED, id);
                        },
                        failure -> logger.log(Level.ERROR, "Persist failed", failure)
                );
    }

    @ConsumeEvent(COMMUNICATION_PERSISTED)
    public void sendMyMessage(Message<UUID> msg) {
        logger.info("Send communication started: " + msg.body());
        communicationService.send(msg.body())
                .subscribe()
                .with(
                        _ -> logger.info("Send completed"),
                        failure -> logger.log(Level.ERROR, "Send failed", failure)
                );
    }


}