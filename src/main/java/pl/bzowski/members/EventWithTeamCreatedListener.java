package pl.bzowski.members;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static pl.bzowski.members.TeamCreatedDto.EVENT_CREATED;

@Singleton
public class EventWithTeamCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(EventWithTeamCreatedListener.class);

    @Inject
    TeamMembersCreator teamMembersCreator;

    @Inject
    EventBus eventBus;

    @ConsumeEvent(EVENT_CREATED)
    public void consumeEventCreated(Message<TeamCreatedDto> message) {
        log.info("[START] method: consumeEventCreated");
        this.teamMembersCreator.createMembersForTeam(message)
                .subscribe()
                .with(
                        _ -> {
                            MembersAssignedDto mad = new MembersAssignedDto(message.body().registeredUserId(), message.body().teamId());
                            log.info("- send: {}: {}", MembersAssignedDto.MEMBERS_ASSIGNED, mad);
                            eventBus.send(MembersAssignedDto.MEMBERS_ASSIGNED, mad);
                            log.info("- send: {}: FINISHED", MembersAssignedDto.MEMBERS_ASSIGNED);
                            log.info("[STOP ] method: consumeEventCreated");
                        },
                        failure -> System.err.println("Błąd: " + failure.getMessage())
                );
    }

}
