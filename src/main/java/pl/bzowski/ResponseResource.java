package pl.bzowski;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/respond")
public class ResponseResource {

    private final Template responseForm;

    public ResponseResource(Template responseForm) {
        this.responseForm = responseForm;
    }


    @GET
    @Path("/{token}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showForm(@PathParam("token") UUID token) {
        PersonQueryLink link = PersonQueryLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku");
        }

        return responseForm.data("link", link);
    }


    @POST
    @Path("/{token}")
    @Consumes("application/x-www-form-urlencoded")
    @Transactional
    public Response submitAnswer(@PathParam("token") UUID token, @FormParam("answer") String answerStr) {
        PersonQueryLink link = PersonQueryLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku " + link);
        }
        try {
            link.answer = PersonQueryLink.Answer.valueOf(answerStr);
            link.persist();
            return Response.ok("Dziękujemy za odpowiedź!").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Nieprawidłowa odpowiedź").build();
        }
    }

}
