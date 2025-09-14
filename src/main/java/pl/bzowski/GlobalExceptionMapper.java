package pl.bzowski;

import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logmanager.Level;

import java.util.logging.Logger;

@Provider
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOG.log(Level.ERROR, "Błąd serwera", exception);

        boolean devMode = LaunchMode.current() == LaunchMode.DEVELOPMENT;

        String message = devMode ? exception.toString() : "Wystąpił błąd serwera";

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{ \"error\": \"" + message + "\" }")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
