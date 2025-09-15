package pl.bzowski.configurations.api;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
    public void postIntegrations(@FormParam(MESSENGER) String messengerRegistrationKey) {
        configurationsRepository.save(MESSENGER, messengerRegistrationKey);
    }

    @POST
    @Path(EMAIL_FROM)
    public void postEmailFrom(@FormParam(EMAIL_FROM) String emailFrom) {
        configurationsRepository.save(EMAIL_FROM, emailFrom);
    }
}
