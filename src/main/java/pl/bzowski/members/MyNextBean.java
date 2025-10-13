package pl.bzowski.members;

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

    private final Logger logger = Logger.getLogger(EventWithTeamCreatedListener.class.getName());

    @Inject
    LinkGenerationResource linkGenerationResource;

    @WithTransaction
    public Uni<List<Person>> getPersonsFromGroups(List<UUID> groupIds) {
        return Person.find("select distinct p from Person p join p.groups g where g.id in ?1", groupIds).list();
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
    public Uni<Void> getVoidUni(Message<TeamCreatedDto> message) {
        TeamCreatedDto dto = message.body();
        UUID teamId = dto.getTeamId();
        switch (dto.getTeamType()) {
            case "group":
                List<UUID> groupIds = dto.getGroupIds();
                if (groupIds != null && !groupIds.isEmpty()) {
                    return getPersonsFromGroups(groupIds)
                            .flatMap(personsFromGroups ->
                                    linkGenerationResource.processTeam(teamId, personsFromGroups)
                            );
                }
                break;
            case "person":
                if (dto.getPersonIds() != null && !dto.getPersonIds().isEmpty()) {
                    return this.getSelectedPersonsFromForm(dto.getPersonIds())
                            .flatMap(selectedPersons -> linkGenerationResource.processTeam(teamId, selectedPersons))
                            .onFailure()
                            .invoke(() -> logger.log(Level.ERROR, "error"));
                }
                break;
            case "all":
                return this.getAllPersonsForUser(dto.getRegisteredUserId())
                        .flatMap(allPersons -> linkGenerationResource.processTeam(teamId, allPersons));
        }
        return Uni.createFrom().voidItem();
    }
}
