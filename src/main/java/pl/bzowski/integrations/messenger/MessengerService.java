package pl.bzowski.integrations.messenger;

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

    public boolean sendMessage(String recipientPsid, String messageText) {
        JsonObject messageJson = Json.createObjectBuilder()
                .add("recipient", Json.createObjectBuilder()
                        .add("id", recipientPsid))
                .add("message", Json.createObjectBuilder()
                        .add("text", messageText))
                .build();

        Response response = messengerRestClient.sendMessage(messageJson.toString(), messengerToken);

        boolean success = false;
        if (response.getStatus() == 200) {
            success = true;
        } else {
            String error = response.readEntity(String.class);
            System.err.println("Błąd przy wysyłce wiadomości do Messengera: " + error);
        }
        response.close();
        return success;
    }
}
