package pl.bzowski.members;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static pl.bzowski.members.TeamCreatedDto.EVENT_CREATED;

@Singleton
public class EventWithTeamCreatedListener {

    @Inject
    MyNextBean myNextBean;

    @Inject
    EventBus eventBus;

    @ConsumeEvent(EVENT_CREATED)
    public void consume(Message<TeamCreatedDto> message) {
        this.myNextBean.getVoidUni(message)
                .subscribe().with(
                unused -> eventBus.send(MembersAssignedDto.MEMBERS_ASSIGNED, new MembersAssignedDto(message.body().getRegisteredUserId(), message.body().getTeamId())),
                failure -> System.err.println("Błąd: " + failure.getMessage())
        );
    }

}
