package pl.bzowski.persons.api;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    public List<Person> listAllPersons() {
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
    public Response deletePerson(@PathParam("id") UUID id) {
        Person person = Person.findById(id);
        if (person == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Osoba nie została znaleziona").build();
        }

        person.delete();
        return Response.noContent().build(); // 204 No Content
    }
}
