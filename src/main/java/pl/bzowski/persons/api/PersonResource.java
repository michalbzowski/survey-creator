package pl.bzowski.persons.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.bzowski.base.ReactiveDelete;
import pl.bzowski.persons.Person;
import pl.bzowski.persons.PersonRepository;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    private final PersonRepository personRepository;

    @Inject
    public PersonResource(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GET
    public Uni<List<Person>> listAllPersons() {
        return personRepository.listAll();
    }

    @POST
    @Transactional
    public Response addPerson(Person person) {
        if (person == null || person.email == null || person.firstName == null || person.lastName == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Brakuje wymaganych danych!").build();
        }
        personRepository.persist(person);

        return Response.status(Response.Status.CREATED).entity(person).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Response> deletePerson(@PathParam("id") UUID id, @FormParam("_method") String method) {
        return ReactiveDelete.reactiveDelete(id, method, Person::findById, "/web/persons");
    }
}
