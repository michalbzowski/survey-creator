package pl.bzowski.events;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.events.web.EventsPageResource;
import pl.bzowski.shared.base.RepositoryBase;
import pl.bzowski.events.web.EventDto;

import java.util.List;
import java.util.UUID;


@Singleton
public class EventRepository extends RepositoryBase {

    private static final Logger log = LoggerFactory.getLogger(EventRepository.class);

    public Uni<List<Event>> findAvailableEvents() {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(uuid -> {
                    Sort localDateTime = Sort.by(
                            "localDateTime",
                            Sort.Direction.Ascending
                    );
                    return Event.list("registeredUserId = ?1 and team is null",
                            localDateTime,
                            uuid);
                });
    }

    @WithTransaction
    public Uni<Event> persist(EventDto eventDto) {
        log.info("[START] method: persist {}", eventDto.toString());
        return currentRegisteredUserId()
                .onItem()
                .invoke(registeredUserId -> log.info("- registeredUserId: {}", registeredUserId))
                .onItem()
                .transformToUni(registeredUserId -> {
                    Event event = new Event(eventDto.name, eventDto.location, eventDto.localDateTime, eventDto.description, registeredUserId);
                    log.info("- Created Event entity: {}", event);
                    return event.<Event>persist()
                            .onItem()
                            .invoke(persistedEvent -> log.info("- Event persisted with ID: {}", persistedEvent.id));
                });
    }

    public Uni<List<Event>> listAll(Sort localDateTime) {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(uuid -> Event.list("registeredUserId = ?1", localDateTime, uuid));
    }

    @WithTransaction
    public Uni<Event> findById(UUID eventId) {
        return Event.findById(eventId);
    }
}
