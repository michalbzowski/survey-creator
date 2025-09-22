package pl.bzowski.communication;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Path("/web/communication/")
public class CommunicationResource {

    @Inject
    @Location("public/confirmed")
    Template confirmed;

    @GET
    @Path("confirm/{id}")
    @WithTransaction
    public Uni<TemplateInstance> confirm(@PathParam("id") UUID id) {
        return CommunicationPersonAgreement.<CommunicationPersonAgreement>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Nie znaleziono potwierdzenia"))
                .flatMap(cpa -> Person.<Person>findById(cpa.personId)
                        .flatMap(person -> {
                            cpa.confirm();
                            return cpa.persistAndFlush()
                                    .replaceWith(() -> confirmed.instance()
                                            .data("firstName", person.firstName)
                                            .data("lastName", person.lastName));
                        })
                );
    }
}
