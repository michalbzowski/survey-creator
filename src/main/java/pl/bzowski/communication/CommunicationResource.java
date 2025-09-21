package pl.bzowski.communication;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Path("/web/communication/")
public class CommunicationResource {

    @Inject
    @Location("public/confirmed")
    Template confirmed;

    @GET
    @Path("confirm/{id}")
    public TemplateInstance confirm(@PathParam("id") UUID id) {
        CommunicationPersonAgreement cpa = CommunicationPersonAgreement.findById(id);
        Person person = Person.findById(cpa.personId);
        cpa.confirm();
        cpa.persist();
        return confirmed.instance()
                .data("firstName", person.firstName)
                .data("lastName", person.lastName);
    }
}
