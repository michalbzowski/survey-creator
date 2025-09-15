package pl.bzowski.integrations.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.Event;
import pl.bzowski.integrations.Integrations;
import pl.bzowski.communication.messenger.MessengerService;
import pl.bzowski.communication.messenger.MessengerUserAgreement;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static pl.bzowski.integrations.Integrations.MESSENGER;

@Singleton
public class IntegrationsRepository extends RepositoryBase {

    private static final Logger logger = Logger.getLogger(IntegrationsRepository.class.getName());

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

    public void findIntegration(String email, List<Event> events, Runnable falback) {
        logger.info(String.format("findIntegreation: %s", email));
        UUID uuid = currentRegisteredUserId();
        logger.info(String.format("registeredUserId: %s", uuid));
        Integrations.find("registeredUserId = ?1", uuid)
                .firstResultOptional()
                .ifPresentOrElse(integrations -> {
                    Integrations cast = (Integrations) integrations;
                    logger.info("Found: " + cast.id + cast.registeredUserId);
                    if (isMessenger(cast)) {
                        MessengerUserAgreement.find("email = ?1 and registeredUserId = ?2 and agree = true ", email, uuid)
                                .firstResultOptional()
                                .ifPresentOrElse(agreement -> {
                                    logger.info("Messenger agreement: " + ((MessengerUserAgreement) agreement).agree);
                                    extracted(events, (MessengerUserAgreement) agreement);
                                }, falback);
                    }
                }, falback);
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
            logger.info("getEventConsumer:" + format);
            messengerService.sendMessage(mua.psid, format);
        };
    }

    private static boolean isMessenger(Integrations integrations) {
        boolean b = integrations.configuration.containsKey(MESSENGER);
        logger.info("isMessenger: " + b);
        return b;
    }
}
