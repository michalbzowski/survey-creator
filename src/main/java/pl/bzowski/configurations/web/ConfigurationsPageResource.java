package pl.bzowski.configurations.web;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import pl.bzowski.configurations.infrastructure.ConfigurationsRepository;

@Path("/web/configurations")
public class ConfigurationsPageResource {

    private final Template configurations;
    private final ConfigurationsRepository configurationsRepository;

    public ConfigurationsPageResource(Template configurations, ConfigurationsRepository configurationsRepository) {
        this.configurations = configurations;
        this.configurationsRepository = configurationsRepository;
    }

    @GET
    @WithTransaction
    public Uni<TemplateInstance> getConfigurations() {
        return configurationsRepository
                .getConfigurations()
                .flatMap(c -> Uni.createFrom().item(this.configurations.data("configurations", c)));
    }
}
