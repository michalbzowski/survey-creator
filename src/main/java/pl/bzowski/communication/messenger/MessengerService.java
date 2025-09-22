package pl.bzowski.communication.messenger;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;


@ApplicationScoped
public class MessengerService {

    @Inject
    @RestClient
    private MessengerRestClient messengerRestClient;

    @ConfigProperty(name = "messenger.token")
    private String messengerToken;

    public Uni<Boolean> sendMessage(String recipientPsid, String messageText) {
        JsonObject messageJson = Json.createObjectBuilder()
                .add("recipient", Json.createObjectBuilder()
                        .add("id", recipientPsid))
                .add("message", Json.createObjectBuilder()
                        .add("text", messageText))
                .build();

        return messengerRestClient.sendMessage(messageJson.toString(), messengerToken)
                .onItem()
                .transform(response -> {
                    Boolean success = Boolean.FALSE;
                    if (response.getStatus() == 200) {
                        success = Boolean.TRUE;
                    } else {
                        String error = response.readEntity(String.class);
                        System.err.println("Błąd przy wysyłce wiadomości do Messengera: " + error);
                    }
                    response.close();
                    return success;
                });

    }
}
