package pl.bzowski.configurations.api;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;

import static pl.bzowski.configurations.Configurations.EMAIL_FROM;
import static pl.bzowski.configurations.Configurations.MESSENGER;

@Path("/api/v1/configurations/")
public class ConfigurationsResource {

    private final ConfigurationsRepository configurationsRepository;

    public ConfigurationsResource(ConfigurationsRepository configurationsRepository) {
        this.configurationsRepository = configurationsRepository;
    }

    @POST
    @Path(MESSENGER)
    public Uni<Response> postIntegrations(@FormParam(MESSENGER) String messengerRegistrationKey) {
        return configurationsRepository.save(MESSENGER, messengerRegistrationKey)
                .onItem()
                .transformToUni(m -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/configurations").build()).build()));
    }

    @POST
    @Path(EMAIL_FROM)
    public Uni<Response> postEmailFrom(@FormParam(EMAIL_FROM) String emailFrom) {
        return configurationsRepository.save(EMAIL_FROM, emailFrom)
                .onItem()
                .transformToUni(m -> Uni.createFrom().item(Response.seeOther(UriBuilder.fromPath("/web/configurations").build()).build()));
    }
}
