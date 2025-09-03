package pl.bzowski.events;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.RequestScoped;
import pl.bzowski.base.RepositoryBase;
import pl.bzowski.events.web.EventDto;

import java.util.List;

@RequestScoped
public class EventRepository extends RepositoryBase {


    public List<Event> findAvailableEvents() {
        return Event.list("registeredUser = ?1 and attendanceList is null", Sort.by("localDateTime", Sort.Direction.Ascending), currentRegisteredUser());
    }

    public Event persist(EventDto eventDto) {
        Event event = new Event(eventDto.name, eventDto.location, eventDto.localDateTime, eventDto.description);
        event.registeredUser = currentRegisteredUser();
        event.persist();
        return event;
    }

    public List<Event> listAll(Sort localDateTime) {
        return Event.list("registeredUser = ?1", Sort.by("localDateTime"), currentRegisteredUser());
    }
}
