//package pl.bzowski.exceptions;
//
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.ext.ExceptionMapper;
//import jakarta.ws.rs.ext.Provider;
//import org.jboss.logging.Logger;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.UUID;
//
//@Provider
//public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
//
//    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);
//
//    @Override
//    public Response toResponse(Throwable exception) {
//        // Generujemy unikalny ID błędu
//        UUID errorId = UUID.randomUUID();
//
//        // Logujemy pełny stos wywołań
//        LOG.errorf(exception, "Nieobsłużony wyjątek (ID błędu: %s)", errorId);
//
//        // Pobieramy stacktrace jako string
//        StringWriter sw = new StringWriter();
//        exception.printStackTrace(new PrintWriter(sw));
//        String stackTrace = sw.toString();
//
//        // Tworzymy odpowiedź JSON ze szczegółami błędu (w prod trybie możesz ograniczyć info)
//        String jsonResponse = String.format("{\"errorId\":\"%s\", \"message\":\"%s\", \"stackTrace\":\"%s\"}",
//                errorId, exception.getMessage(), stackTrace);
//
//        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                .entity(jsonResponse)
//                .type(MediaType.APPLICATION_JSON)
//                .build();
//    }
//}
