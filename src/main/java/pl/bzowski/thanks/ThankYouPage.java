package pl.bzowski.thanks;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pl.bzowski.tags.Tag;

@Path("/web/thank_you")
public class ThankYouPage {

    private final Template thankYou;

    public ThankYouPage(Template thankYou) {
        this.thankYou = thankYou;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        return thankYou.instance();
    }
}
