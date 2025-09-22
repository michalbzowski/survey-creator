package pl.bzowski.communication.messenger;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/me/messages")
@RegisterRestClient(configKey="messenger-api")
public interface MessengerRestClient {

    public static final String INSTRUKCJA = """
            Nie rozumiem Twojego polecenia. Nie jestem inteligenty. Potrzebuję precyzyjnego komunikatu:
            "ZAPISZ MNIE: tu_klucz_twojego_zespolu"
            lub
            "WYPISZ MNIE: tu_klucz_twojego_zespolu"
            """;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> sendMessage(String messagePayload, @QueryParam("access_token") String accessToken);
}
