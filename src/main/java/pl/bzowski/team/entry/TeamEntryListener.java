package pl.bzowski.team.entry;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static pl.bzowski.team.entry.TeamCreatedDto.EVENT_WITH_TEAM_CREATED;

@Singleton
public class TeamEntryListener {

    @Inject
    MyNextBean myNextBean;

    @ConsumeEvent(EVENT_WITH_TEAM_CREATED)
    public void consume(Message<TeamCreatedDto> message) {
        this.myNextBean.getVoidUni(message).subscribe().with(
                unused -> System.out.println("Operacja zakończona powodzeniem"),
                failure -> System.err.println("Błąd: " + failure.getMessage())
        );
    }

}
