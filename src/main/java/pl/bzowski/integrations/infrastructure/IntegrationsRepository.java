package pl.bzowski.integrations.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.integrations.Integrations;
import pl.bzowski.integrations.messenger.MessengerService;
import pl.bzowski.integrations.messenger.MessengerUserAgreement;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static pl.bzowski.integrations.api.IntegrationsResource.MESSENGER;

@Singleton
public class IntegrationsRepository extends RepositoryBase {

    MessengerService messengerService;

    public Map<String, Object> getConfiguration() {
        PanacheQuery<Integrations> panacheEntityBasePanacheQuery = Integrations.find("registeredUserId = ?1", currentRegisteredUserId());
        return panacheEntityBasePanacheQuery
                .firstResultOptional()
                .map((Integrations value) -> value.configuration)
                .orElse(Map.of());
    }

    @Transactional
    public void save(String jsonKey, Object jsonValue) {
        Integrations.find("registeredUserId = ?1", currentRegisteredUserId())
                .firstResultOptional().
                ifPresentOrElse(
                        value -> {
                            ((Integrations) value).configuration.put(jsonKey, jsonValue);
                            value.persist();
                        },
                        () -> {
                            Integrations integrations = new Integrations();
                            integrations.registeredUserId = currentRegisteredUserId();
                            integrations.configuration = Map.of(jsonKey, jsonValue);
                            integrations.persist();
                        });
    }

    public Uni<Void> findIntegration(String email, List<Event> events, Runnable falback) {
        Integrations.find("registeredUserId = ?1", currentRegisteredUserId())
                .firstResultOptional()
                .ifPresentOrElse(integrations -> {
                    Integrations cast = (Integrations) integrations;
                    if (isMessenger(cast)) {
                        MessengerUserAgreement.find("email = ?1 and registeredUserId = ?2 and agree = true ", email, currentRegisteredUserId())
                                .firstResultOptional()
                                .ifPresentOrElse(agreement -> extracted(events, (MessengerUserAgreement) agreement), falback);
                    }
                }, falback);
        return Uni.createFrom().nullItem();
    }

    private void extracted(List<Event> events, MessengerUserAgreement agreement) {
        MessengerUserAgreement mua = agreement;
        getaVoid(events, mua);
    }

    private void getaVoid(List<Event> events, MessengerUserAgreement mua) {
        events.forEach(getEventConsumer(mua));
    }

    private Consumer<Event> getEventConsumer(MessengerUserAgreement mua) {
        return event -> {
            String format = String.format("Nazwa: %s\n\nOpis: %s\n\n Kiedy: %s\n\nGdzie: %s",
                    event.name, event.description, event.formatedLocalDateTime(), event.location);
            messengerService.sendMessage(mua.psid, format);
        };
    }

    private static boolean isMessenger(Integrations integrations) {
        return integrations.configuration.containsKey(MESSENGER);
    }
}
