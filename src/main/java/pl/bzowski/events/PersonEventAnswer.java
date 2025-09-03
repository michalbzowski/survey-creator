package pl.bzowski.events;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.persons.Person;
import pl.bzowski.attendance_list.AttendanceList;

import java.util.UUID;

@Entity
@Table(name = "person_event_answers")
public class PersonEventAnswer extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    public Person person;

    @ManyToOne
    @JoinColumn(name = "attendance_list_id")
    public AttendanceList attendanceList;

    @ManyToOne
    @JoinColumn(name = "event_id")
    public Event event;

    @Enumerated(EnumType.STRING)
    public Answer answer;

    public enum Answer {
        TAK, NIE, ODPOWIEM_POZNIEJ
    }
}
