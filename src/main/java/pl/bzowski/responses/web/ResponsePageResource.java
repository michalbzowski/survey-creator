package pl.bzowski.responses.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.bzowski.links.PersonSurveyLink;

import java.util.UUID;

@Path("/web/responses")
public class ResponsePageResource {

    private final Template responseForm;
    private final Template thankYou;
    private final Template error;

    public ResponsePageResource(Template responseForm, Template thankYou, Template error) {
        this.responseForm = responseForm;
        this.thankYou = thankYou;
        this.error = error;
    }

    @GET
    @Path("/{token}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showForm(@PathParam("token") UUID token) {
        PersonSurveyLink link = PersonSurveyLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku");
        }

        return responseForm.data("link", link);
    }

    @POST
    @Path("/{token}")
    @Consumes("application/x-www-form-urlencoded")
    @Transactional
    public TemplateInstance submitAnswer(@PathParam("token") UUID token, @FormParam("answer") String answerStr) {
        PersonSurveyLink link = PersonSurveyLink.find("linkToken", token).firstResult();
        if (link == null) {
            throw new NotFoundException("Nie znaleziono linku " + link);
        }
        try {
            link.answer = PersonSurveyLink.Answer.valueOf(answerStr);
            link.persist();
            return thankYou.instance();
        } catch (IllegalArgumentException e) {
            return error.instance();
        }
    }
}
