package pl.bzowski.members;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.persons.Person;

import java.util.List;
import java.util.UUID;

@Singleton
public class TeamMembersCreator {

    private static final Logger log = LoggerFactory.getLogger(TeamMembersCreator.class);

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
    public Uni<Void> createMembersForTeam(Message<TeamCreatedDto> message) {
        log.info("[START] method: createMembersForTeam");
        TeamCreatedDto dto = message.body();
        log.info("- body: {}", dto);
        UUID teamId = dto.teamId();
        log.info("- teamId: {}", teamId);
        switch (dto.teamType()) {
            case "group":
                List<UUID> groupIds = dto.groupIds();
                if (groupIds != null && !groupIds.isEmpty()) {
                    return getPersonsFromGroups(groupIds)
                            .flatMap(personsFromGroups ->
                                    linkGenerationResource.processTeam(teamId, personsFromGroups)
                            );
                }
                break;
            case "person":
                if (dto.personIds() != null && !dto.personIds().isEmpty()) {
                    return this.getSelectedPersonsFromForm(dto.personIds())
                            .flatMap(selectedPersons -> linkGenerationResource.processTeam(teamId, selectedPersons))
                            .onFailure()
                            .invoke(() -> log.error("error"));
                }
                break;
            case "all":
                return this.getAllPersonsForUser(dto.registeredUserId())
                        .flatMap(allPersons -> linkGenerationResource.processTeam(teamId, allPersons));
        }
        return Uni.createFrom().voidItem();
    }
}
