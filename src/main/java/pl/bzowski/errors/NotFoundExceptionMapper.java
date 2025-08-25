package pl.bzowski.errors;

import io.quarkus.qute.Location;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@Provider
@ApplicationScoped
public class NotFoundExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<NotFoundException> {

    @Inject
    @Location("errors/404.html")
    Template notFoundTemplate;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException exception) {
        String path = uriInfo.getPath();
        return Response.status(Response.Status.NOT_FOUND)
                .entity(notFoundTemplate.data("path", path))
                .type(MediaType.TEXT_HTML)
                .build();
    }
}
