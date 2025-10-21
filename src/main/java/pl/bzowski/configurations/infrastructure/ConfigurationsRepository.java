package pl.bzowski.configurations.infrastructure;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.shared.base.RepositoryBase;
import pl.bzowski.configurations.Configurations;
import pl.bzowski.events.Event;
import pl.bzowski.messaging.messenger.MessengerService;
import pl.bzowski.messaging.messenger.MessengerUserAgreement;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static pl.bzowski.configurations.Configurations.EMAIL_FROM;
import static pl.bzowski.configurations.Configurations.MESSENGER;

@Singleton
public class ConfigurationsRepository extends RepositoryBase {

    private static final Logger logger = Logger.getLogger(ConfigurationsRepository.class.getName());

    MessengerService messengerService;

    @WithTransaction
    public Uni<Map<String, Object>> getConfigurations() {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(uuid -> Configurations.<Configurations>find("registeredUserId = ?1", uuid)
                        .firstResult()
                        .flatMap(value -> {
                            if (value != null) {
                                return Uni.createFrom().item(value.configuration);
                            } else {
                                return Uni.createFrom().item(Map.of());
                            }
                        }));
    }

    @WithTransaction
    public Uni<Void> save(String jsonKey, Object jsonValue) {
        return currentRegisteredUserId()
                .flatMap(registeredUserId ->
                        Configurations.<Configurations>find("registeredUserId = ?1", registeredUserId)
                                .firstResult()
                                .flatMap(config -> {
                                    if (config != null) {
                                        config.configuration.put(jsonKey, jsonValue);
                                        return config.persistAndFlush().replaceWithVoid();
                                    } else {
                                        return piesek(registeredUserId, jsonKey, jsonValue);
                                    }
                                }));
    }

    private Uni<Void> piesek(UUID registeredUserId, String jsonKey, Object jsonValue) {
        Configurations newConfig = new Configurations();
        newConfig.registeredUserId = registeredUserId;
        newConfig.configuration = Map.of(jsonKey, jsonValue);
        return newConfig.persistAndFlush().replaceWithVoid();
    }


    public Uni<Void> findIntegration(String email, List<Event> events) {
        logger.info(String.format("findIntegreation: %s", email));
        return currentRegisteredUserId().flatMap(registeredUserId -> {
                    logger.info(String.format("registeredUserId: %s", registeredUserId));
                    return Configurations.find("registeredUserId = ?1", registeredUserId)
                            .firstResult()
                            .flatMap(integrations -> {
                                Configurations cast = (Configurations) integrations;
                                logger.info("Found: " + cast.id + cast.registeredUserId);
                                if (isMessenger(cast)) {
                                    return MessengerUserAgreement.find("email = ?1 and registeredUserId = ?2 and agree = true ", email, registeredUserId)
                                            .firstResult()
                                            .flatMap(agreement -> {
                                                logger.info("Messenger agreement: " + ((MessengerUserAgreement) agreement).agree);
                                                extracted(events, (MessengerUserAgreement) agreement);
                                                return Uni.createFrom().voidItem();
                                            });
                                }
                                return Uni.createFrom().voidItem();
                            });
                }

        );
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

    private static boolean isMessenger(Configurations configurations) {
        boolean b = configurations.configuration.containsKey(MESSENGER);
        logger.info("isMessenger: " + b);
        return b;
    }

    @WithTransaction
    public Uni<Map<String, Object>> getConfigurationsForUser(UUID currentUserId) {
        return Uni.createFrom().item(Map.of(EMAIL_FROM, "Twój KOT!"));
    }
}
