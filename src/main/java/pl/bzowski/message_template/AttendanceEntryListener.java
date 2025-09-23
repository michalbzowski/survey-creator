package pl.bzowski.message_template;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logmanager.Level;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static pl.bzowski.message_template.AttendanceCreatedDto.EVENT_WITH_ATTENDANCE_CREATED;

@Singleton
public class AttendanceEntryListener {

    @WithTransaction
    public Uni<List<Person>> getPersonsFromGroups(List<UUID> groupIds) {
        return Person.find("select p from Person p join p.groups g where g.id in ?1", groupIds).list();
    }

    @WithTransaction
    public Uni<List<Person>> getSelectedPersonsFromForm(List<UUID> personIds) {
        return Person.find("id in ?1", personIds).list();
    }

    @WithTransaction
    public Uni<List<Person>> getAllPersonsForUser(UUID registeredUserId) {
        return Person.find("registeredUserId in ?1", registeredUserId).list();
    }

    private final Logger logger = Logger.getLogger(AttendanceEntryListener.class.getName());

    @Inject
    LinkGenerationResource linkGenerationResource;

    @ConsumeEvent(EVENT_WITH_ATTENDANCE_CREATED)
    public void consume(Message<AttendanceCreatedDto> message) {
        AttendanceCreatedDto dto = message.body();
        UUID attendanceListId = dto.getAttendanceListId();
        switch (dto.getAttendanceType()) {
            case "group":
                List<UUID> groupIds = dto.getGroupIds();
                if (groupIds != null && !groupIds.isEmpty()) {
                    this.getPersonsFromGroups(groupIds)
                            .onItem()
                            .invoke(personsFromGroups -> linkGenerationResource.processAttendanceList(attendanceListId, personsFromGroups));
                }
                break;
            case "person":
                if (dto.getPersonIds() != null && !dto.getPersonIds().isEmpty()) {
                    this.getSelectedPersonsFromForm(dto.getPersonIds())
                            .onItem()
                            .invoke(selectedPersons -> linkGenerationResource.processAttendanceList(attendanceListId, selectedPersons))
                            .onFailure()
                            .invoke(() ->logger.log(Level.ERROR, "error"));
                }
                break;
            case "all":
                this.getAllPersonsForUser(dto.getRegisteredUserId())
                        .onItem()
                        .invoke(allPersons -> linkGenerationResource.processAttendanceList(attendanceListId, allPersons));
                break;
        }
    }
}
