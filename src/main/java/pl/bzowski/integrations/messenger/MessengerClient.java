package pl.bzowski.integrations.messenger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@ApplicationScoped
public class MessengerClient {


    @ConfigProperty(name = "messenger.token")
    String messengerToken;

    private static final String FACEBOOK_GRAPH_API_URL = "https://graph.facebook.com/v16.0/me/messages";

    public boolean sendMessage(String recipientPsid, String messageText) {
        Client client = ClientBuilder.newClient();

        JsonObject messageJson = Json.createObjectBuilder()
                .add("recipient", Json.createObjectBuilder()
                        .add("id", recipientPsid))
                .add("message", Json.createObjectBuilder()
                        .add("text", messageText))
                .build();

        Response response = client.target(FACEBOOK_GRAPH_API_URL)
                .queryParam("access_token", messengerToken)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(messageJson.toString()));

        boolean success = false;
        if (response.getStatus() == 200) {
            success = true;
        } else {
            String error = response.readEntity(String.class);
            System.err.println("Błąd przy wysyłce wiadomości do Messengera: " + error);
        }

        response.close();
        client.close();
        return success;
    }
}
