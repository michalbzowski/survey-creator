package pl.bzowski.communication.messenger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.LoggerFactory;
import pl.bzowski.configurations.Configurations;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static pl.bzowski.configurations.Configurations.MESSENGER;
import static pl.bzowski.communication.messenger.MessengerRestClient.INSTRUKCJA;
import static pl.bzowski.communication.messenger.MyParser.parseEmailFromText;
import static pl.bzowski.communication.messenger.MyParser.parseUuidFromText;

@Path("/api/messenger/webhook")
public class MessengerWebhookResource {

    private static final Logger logger = Logger.getLogger(MessengerWebhookResource.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MessengerWebhookResource.class);

    @ConfigProperty(name = "messenger.token")
    String messengerToken;

    @Inject
    MessengerService messengerService;


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
        // Parsuj JSON i wyciągnij sender.id oraz text message
        String psid = MessengerPayloadParser.extractPsidFromPayload(payload);
        String text = MessengerPayloadParser.extractMessageText(payload);

        if (text != null) {
            manageActions(psid, text.trim());
        }
        return Response.ok().build();
    }

    public Uni<Void> manageActions(String psid, String trimmedText) {
        logger.info(trimmedText);

        if (trimmedText.startsWith("ZAPISZ MNIE:")) {
            logger.info("Parsowanie - start");
            UUID messengerRegistrationKey = parseUuidFromText(trimmedText);
            logger.info("messengerRegistrationKey: " + messengerRegistrationKey);
            String email = parseEmailFromText(trimmedText);
            logger.info("email: " + email);

            if (messengerRegistrationKey != null) {
                // Zapisz powiązanie psid <-> uuid w bazie reaktywnie
                return saveUserMapping(psid, email, messengerRegistrationKey, true)
                        .flatMap(saved -> {
                            if (saved) {
                                logger.info("Zapisano ZAPISZ MNIE: " + psid + email + messengerRegistrationKey);
                                messengerService.sendMessage(psid, "Zgoda zapisana. Będziesz otrzymywać powiadomienia.");
                            }
                            return Uni.createFrom().voidItem();
                        });
            } else {
                messengerService.sendMessage(psid, INSTRUKCJA);
                return Uni.createFrom().voidItem();
            }
        } else if (trimmedText.startsWith("WYPISZ MNIE:")) {
            logger.info("WYPISZ MNIE command recognized");
            UUID messengerRegistrationKey = parseUuidFromText(trimmedText);
            String email = parseEmailFromText(trimmedText);
            return saveUserMapping(psid, email, messengerRegistrationKey, false)
                    .flatMap(saved -> {
                        if (saved) {
                            messengerService.sendMessage(psid, "Zgoda anulowana. Nie będziesz otrzymywać powiadomienia.");
                        }
                        return Uni.createFrom().voidItem();
                    });
        } else {
            logger.info("Nie ma słów kluczowych. Wysylam instrukcje!");
            messengerService.sendMessage(psid, INSTRUKCJA);
            return Uni.createFrom().voidItem();
        }
    }


    public Uni<Boolean> saveUserMapping(String psid, String email, UUID messengerRegistrationKey, boolean agree) {
        String nativeQuery = "SELECT * FROM integrations WHERE configuration->>'" + MESSENGER + "' = :key";
        return Panache.getSession().flatMap(session ->
                session.createNativeQuery(nativeQuery, Configurations.class)
                        .setParameter("key", messengerRegistrationKey.toString())
                        .getResultList()
                        .flatMap(configurationsList -> {
                            if (configurationsList.isEmpty()) {
                                // brak wyników - wyślij wiadomość i zwróć false
                                messengerService.sendMessage(psid, "Nie znalazłem takiego klucza. Wróć do szefa swojego zespołu");
                                return Uni.createFrom().item(false);
                            }
                            // dla każdego znalezionego Configurations utwórz MessengerUserAgreement i zapisz
                            List<Uni<Void>> persistUnis = configurationsList.stream().map(config -> {
                                MessengerUserAgreement agreement = new MessengerUserAgreement();
                                agreement.psid = psid;
                                agreement.registeredUserId = config.registeredUserId;
                                agreement.email = email;
                                agreement.messengerRegistrationKey = messengerRegistrationKey;
                                agreement.agree = agree;
                                return agreement.persistAndFlush().replaceWithVoid();
                            }).toList();

                            // Po zapisaniu wszystkich zwróć true
                            return Uni.combine().all().unis(persistUnis).discardItems().replaceWith(true);
                        })
        );
    }

    static class MessengerPayloadParser {

        public static String extractPsidFromPayload(String payload) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(payload))) {
                JsonObject jsonObject = jsonReader.readObject();
                // Navigujemy po ścieżce -> entry[0] -> messaging[0] -> sender -> id
                return jsonObject.getJsonArray("entry")
                        .getJsonObject(0)
                        .getJsonArray("messaging")
                        .getJsonObject(0)
                        .getJsonObject("sender")
                        .getString("id");
            }
        }

        public static String extractMessageText(String payload) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(payload))) {
                JsonObject jsonObject = jsonReader.readObject();
                // Navigujemy po ścieżce -> entry[0] -> messaging[0] -> message -> text
                JsonObject messagingObj = jsonObject.getJsonArray("entry")
                        .getJsonObject(0)
                        .getJsonArray("messaging")
                        .getJsonObject(0);

                if (messagingObj.containsKey("message")) {
                    JsonObject messageObj = messagingObj.getJsonObject("message");
                    if (messageObj.containsKey("text")) {
                        return messageObj.getString("text");
                    }
                }
                return null; // Brak tekstu w wiadomości
            }
        }
    }

}
