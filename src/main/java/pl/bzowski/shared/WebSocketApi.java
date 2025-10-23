package pl.bzowski.shared;

import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import pl.bzowski.members.MembersAssignedDto;

import java.util.UUID;


@WebSocket(path = "/wss/{registeredUserId}")
public class WebSocketApi {

    private static final Logger LOG = Logger.getLogger(WebSocketApi.class);

    // Declare the type of messages that can be sent and received
    public enum MessageType {USER_JOINED, USER_LEFT, CHAT_MESSAGE}

    public record WebSocketMessage(MessageType type, String from, String message) {
    }

    @Inject
    WebSocketConnection connection;

    @Inject
    OpenConnections openConnections;

    @OnOpen
    public WebSocketMessage onOpen() {
        String from = connection.pathParam("registeredUserId");
        LOG.info(String.format("From: %s", from));
        LOG.info(String.format("Connections: %s", connection.getOpenConnections().size()));
        return new WebSocketMessage(MessageType.USER_JOINED, from, null);
    }

    @OnClose
    public void onClose() {
        WebSocketMessage departure = new WebSocketMessage(MessageType.USER_LEFT, connection.pathParam("registeredUserId"), null);
        connection.broadcast().sendTextAndAwait(departure);
    }

    @OnTextMessage
    public WebSocketMessage onMessage(WebSocketMessage message) {
        return message;
    }

    @ConsumeEvent(MembersAssignedDto.MEMBERS_ASSIGNED)
    public void onMembersAssigned(MembersAssignedDto membersAssignedDto) {
        openConnections.listAll()
                .stream()
                .filter(c -> {
                    String registeredUserId1 = c.pathParam("registeredUserId");
                    UUID registeredUserId2 = membersAssignedDto.getRegisteredUserId();
                    return registeredUserId1.equals(registeredUserId2.toString());
                })
                .findFirst().ifPresentOrElse(c -> c.sendText("Hejeczka")
                                .subscribe()
                                .with(_ -> System.out.println("Success"),
                                        f -> System.out.println("Failure"))
                        , () -> System.out.println("lol"));
    }
}
