package pl.bzowski;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/queries")
public class QueryDetailsResource {

    private final Template queryDetails;

    public QueryDetailsResource(Template queryDetails) {
        this.queryDetails = queryDetails;
    }

    @GET
    @Path("/{id}/details")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showQueryDetails(@PathParam("id") UUID id) {
        Query query = Query.findById(id);
        if (query == null) {
            throw new NotFoundException("Nie znaleziono zapytania");
        }

        List<PersonQueryLink> links = PersonQueryLink.find("query", query).list();

        return queryDetails
                .data("query", query)
                .data("links", links);
    }
}
