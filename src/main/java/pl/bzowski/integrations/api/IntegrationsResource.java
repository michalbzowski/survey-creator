package pl.bzowski.integrations.api;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import pl.bzowski.integrations.infrastructure.IntegrationsRepository;

@Path("/api/v1/integrations/")
public class IntegrationsResource {

    public static final String MESSENGER = "messengerRegistrationKey";

    private final IntegrationsRepository integrationsRepository;

    public IntegrationsResource(IntegrationsRepository integrationsRepository) {
        this.integrationsRepository = integrationsRepository;
    }

    @POST
    @Path(MESSENGER)
    public void postIntegrations(@FormParam(MESSENGER) String messengerRegistrationKey) {
        integrationsRepository.save(MESSENGER, messengerRegistrationKey);
    }
}
