package pl.bzowski.attendances.entry;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static pl.bzowski.attendances.entry.AttendanceCreatedDto.EVENT_WITH_ATTENDANCE_CREATED;

@Singleton
public class AttendanceEntryListener {

    @Inject
    MyNextBean myNextBean;

    @ConsumeEvent(EVENT_WITH_ATTENDANCE_CREATED)
    public void consume(Message<AttendanceCreatedDto> message) {
        this.myNextBean.getVoidUni(message).subscribe().with(
                unused -> System.out.println("Operacja zakończona powodzeniem"),
                failure -> System.err.println("Błąd: " + failure.getMessage())
        );
    }

}
