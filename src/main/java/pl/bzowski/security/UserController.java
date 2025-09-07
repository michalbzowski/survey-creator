package pl.bzowski.security;

import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pl.bzowski.base.RepositoryBase;

@Path("/sec/")
@Singleton
public class UserController extends RepositoryBase {

    @ConfigProperty(name = "app.host")
    String appHost;

    @GET
    @Path("username")
    public Response getCurrentUserUsername() {
        return Response.ok(super.currentUsername()).build();
    }

    @GET
    @Path("/logout")
    public Response logout() {
        // Usuwamy ciasteczka przez ustawienie pustej wartości i daty wygaśnięcia w przeszłości
        NewCookie removeQuarkusCredential = new NewCookie("quarkus-credential", "", "/", null, null, 0, false);
        NewCookie removeQSession = new NewCookie("q_session_chunk_1", "", "/", null, null, 0, false);

        String keycloakLogoutUrl = appHost + "/realms/master/protocol/openid-connect/logout?redirect_uri=http%3A%2F%2Flocalhost:8080%2Flogged-out";

        Response.ResponseBuilder response = Response.seeOther(java.net.URI.create(keycloakLogoutUrl));
        response.cookie(removeQuarkusCredential);
        response.cookie(removeQSession);

        return response.build();
    }
}
