package pl.bzowski;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/queries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryResource {

    @GET
    public List<Query> listAllQueries() {
        return Query.listAll();
    }

    @POST
    @Transactional
    public Response createQuery(Query query) {
        if (query == null || query.title == null || query.description == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Brakuje wymaganych danych").build();
        }

        query.persist();
        return Response.status(Response.Status.CREATED).entity(query).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteQuery(@PathParam("id") UUID id) {
        Query query = Query.findById(id);
        if (query == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Query nie została znaleziona").build();
        }

        query.delete();
        return Response.noContent().build(); // 204 No Content
    }
}
