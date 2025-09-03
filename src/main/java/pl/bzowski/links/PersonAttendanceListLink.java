package pl.bzowski.links;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "person_attendance_list_links")
public class PersonAttendanceListLink extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column
    public UUID personId;

    @Column
    public String personFirstName;

    @Column
    public String personLastName;

    @Column
    public String personEmail;

    @Column
    public String personTag;

    @ManyToOne(optional = false)
    public AttendanceList attendanceList;

    @Column(nullable = false)
    public UUID attendanceListId;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Column(nullable = false)
    public Boolean attendanceListAnswered = false;

    public void sendingError() {
        this.status = SendingStatus.ERROR;
    }

    public enum SendingStatus {
        TO_SEND, SENT, ERROR
    }

    @Enumerated(EnumType.STRING)
    public SendingStatus status;

    public PersonAttendanceListLink() {
    }

    public PersonAttendanceListLink(Person person, AttendanceList attendanceList) {
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.attendanceList = attendanceList;
        this.attendanceListId = attendanceList.id;
        this.linkToken = UUID.randomUUID();
        this.status = SendingStatus.TO_SEND;
    }

    public void sent() {
        status = SendingStatus.SENT;
    }
}
