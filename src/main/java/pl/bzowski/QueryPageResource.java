package pl.bzowski;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.UUID;

@Path("/queries")
public class QueryPageResource {

    private final Template addQuery;
    private final Template listQueries;

    public QueryPageResource(Template addQuery, Template listQueries) {
        this.addQuery = addQuery;
        this.listQueries = listQueries;
    }

    @GET
    @Path("/add")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showAddForm() {
        return addQuery.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response addQuery(@FormParam("title") String title, @FormParam("description") String description) {
        Query query = new Query(title, description);
        query.persist();
        return Response.seeOther(UriBuilder.fromPath("/queries/list").build()).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listQueries() {
        List<Query> queries = Query.listAll();
        return listQueries.data("queries", queries);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response deleteQuery(@PathParam("id") UUID id, @FormParam("_method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            Query query = Query.findById(id);
            if (query != null) {
                query.delete();
            }
        }
        return Response.seeOther(UriBuilder.fromPath("/queries/list").build()).build();
    }
}
