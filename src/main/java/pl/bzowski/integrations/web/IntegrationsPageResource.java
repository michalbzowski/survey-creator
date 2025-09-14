package pl.bzowski.integrations.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import pl.bzowski.integrations.infrastructure.IntegrationsRepository;

import java.util.Map;

@Path("/web/integrations")
public class IntegrationsPageResource {

    private final Template integrations;
    private final IntegrationsRepository integrationsRepository;

    public IntegrationsPageResource(Template integrations, IntegrationsRepository integrationsRepository) {
        this.integrations = integrations;
        this.integrationsRepository = integrationsRepository;
    }

    @GET
    public TemplateInstance getIntegrations() {
        Map<String, Object> configuration = integrationsRepository.getConfiguration();
        return integrations.data("integrations", configuration);
    }
}
