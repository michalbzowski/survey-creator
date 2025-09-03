package pl.bzowski.security;

import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;

@Path("/")
public class PageResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response redirectFromEmptyPath() {
        URI build = UriBuilder.fromPath("/web/events/").build();
        return Response.seeOther(build).build();
    }
}
