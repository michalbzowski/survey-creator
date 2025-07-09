package pl.bzowski;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;

@Path("/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinkGenerationResource {

    @GET
    public List<PersonQueryLink> listAllLinks() {
        return PersonQueryLink.listAll();
    }

    @GET
    @Path("/generate/{queryId}")
    @Transactional
    public Response generateLinks(@PathParam("queryId") UUID queryId) {
        Query query = Query.findById(queryId);
        if (query == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Zapytanie nie istnieje").build();
        }

        List<Person> persons = Person.listAll();
        for (Person person : persons) {
            // Sprawdź, czy link już istnieje
            boolean exists = PersonQueryLink.find("person = ?1 and query = ?2", person, query).firstResultOptional().isPresent();
            if (!exists) {
                PersonQueryLink link = new PersonQueryLink(person, query);
                link.persist();
            }
        }

        // Przekierowanie do widoku szczegółów zapytania
        return Response.seeOther(UriBuilder.fromPath("/queries/{id}/details").build(queryId)).build();

    }
}
