package pl.bzowski.events;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import pl.bzowski.shared.base.RepositoryBase;
import pl.bzowski.events.web.EventDto;

import java.util.List;


@Singleton
public class EventRepository extends RepositoryBase {

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
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(registeredUserId -> {
                    Event event = new Event(eventDto.name, eventDto.location, eventDto.localDateTime, eventDto.description, registeredUserId);
                    return event.persist();
                });
    }

    public Uni<List<Event>> listAll(Sort localDateTime) {
        return currentRegisteredUserId()
                .onItem()
                .transformToUni(uuid -> Event.list("registeredUserId = ?1", localDateTime, uuid));
    }
}
