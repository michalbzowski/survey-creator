package pl.bzowski.integrations.messenger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/messenger/webhook")
public class MessengerWebhookResource {

  @ConfigProperty(name = "messenger.token")
  String messengerToken;

  @GET
  public Response verifyWebhook(@QueryParam("hub.mode") String mode,
                                @QueryParam("hub.verify_token") String verifyToken,
                                @QueryParam("hub.challenge") String challenge) {
    if ("subscribe".equals(mode) && messengerToken.equals(verifyToken)) {
      return Response.ok(challenge).build(); // potwierdzenie weryfikacji
    }
    return Response.status(403).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response receiveMessage(String payload) {
    // Parsuj JSON wiadomości przychodzących od Messengera,
    // obsłuż quick replies ("tak", "nie", "później") i inne eventy,
    // np. zapisz odpowiedzi do bazy lub podjęcie akcji
    System.out.println("Received from messenger: " + payload);
    return Response.ok().build();
  }
}
