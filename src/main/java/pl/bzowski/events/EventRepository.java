package pl.bzowski.events;

import io.quarkus.panache.common.Sort;
import jakarta.inject.Singleton;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.web.EventDto;

import java.util.List;

@Singleton
public class EventRepository extends RepositoryBase {


    public List<Event> findAvailableEvents() {
        return Event.list("registeredUserId = ?1 and attendanceList is null", Sort.by("localDateTime", Sort.Direction.Ascending), currentRegisteredUserId());
    }

    public Event persist(EventDto eventDto) {
        Event event = new Event(eventDto.name, eventDto.location, eventDto.localDateTime, eventDto.description);
        event.registeredUserId = currentRegisteredUserId();
        event.persist();
        return event;
    }

    public List<Event> listAll(Sort localDateTime) {
        return Event.list("registeredUserId = ?1", localDateTime, currentRegisteredUserId());
    }
}
