package pl.bzowski.attendances.entry;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.attendances.list.AttendanceList;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "person_attendance_list_links")
public class AttendanceEntry extends PanacheEntityBase {

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

    public AttendanceEntry() {
    }

    public AttendanceEntry(Person person, AttendanceList attendanceList) {
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.attendanceList = attendanceList;
        this.attendanceListId = attendanceList.id;
        this.linkToken = UUID.randomUUID();
    }
}
