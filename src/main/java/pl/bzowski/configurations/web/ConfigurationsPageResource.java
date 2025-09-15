package pl.bzowski.configurations.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;

import java.util.Map;

@Path("/web/configurations")
public class ConfigurationsPageResource {

    private final Template configurations;
    private final ConfigurationsRepository configurationsRepository;

    public ConfigurationsPageResource(Template configurations, ConfigurationsRepository configurationsRepository) {
        this.configurations = configurations;
        this.configurationsRepository = configurationsRepository;
    }

    @GET
    public TemplateInstance getConfigurations() {
        Map<String, Object> configurations = configurationsRepository.getConfigurations();
        return this.configurations.data("configurations", configurations);
    }
}
