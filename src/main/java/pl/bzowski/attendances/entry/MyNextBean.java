package pl.bzowski.attendances.entry;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logmanager.Level;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class MyNextBean {

    private final Logger logger = Logger.getLogger(AttendanceEntryListener.class.getName());

    @Inject
    LinkGenerationResource linkGenerationResource;

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

    @WithTransaction
    public Uni<Void> getVoidUni(Message<AttendanceCreatedDto> message) {
        AttendanceCreatedDto dto = message.body();
        UUID attendanceListId = dto.getAttendanceListId();
        switch (dto.getAttendanceType()) {
            case "group":
                List<UUID> groupIds = dto.getGroupIds();
                if (groupIds != null && !groupIds.isEmpty()) {
                    return getPersonsFromGroups(groupIds)
                            .flatMap(personsFromGroups ->
                                    linkGenerationResource.processAttendanceList(attendanceListId, personsFromGroups)
                            );
                }
                break;
            case "person":
                if (dto.getPersonIds() != null && !dto.getPersonIds().isEmpty()) {
                    return this.getSelectedPersonsFromForm(dto.getPersonIds())
                            .onItem()
                            .invoke(selectedPersons -> linkGenerationResource.processAttendanceList(attendanceListId, selectedPersons))
                            .onFailure()
                            .invoke(() -> logger.log(Level.ERROR, "error"))
                            .replaceWithVoid();
                }
                break;
            case "all":
                return this.getAllPersonsForUser(dto.getRegisteredUserId())
                        .onItem()
                        .invoke(allPersons -> linkGenerationResource.processAttendanceList(attendanceListId, allPersons))
                        .replaceWithVoid();
        }
        return Uni.createFrom().voidItem();
    }
}
