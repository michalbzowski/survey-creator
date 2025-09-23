package pl.bzowski.message_template;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.attendance_list.AttendanceList;
import pl.bzowski.communication.SendingStatus;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "person_attendance_list_links")//TODO: AttendanceEntry
public class MessageTemplate extends PanacheEntityBase {

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

//    public CommunicationDto toCommunicationDto() {
//        return new CommunicationDto(
//                this.personId,
//                this.personFirstName,
//                this.personLastName,
//                this.personEmail,
//                this.status,
//                Map.of("attendanceListId", attendanceListId)
//        );
//    }

    @Enumerated(EnumType.STRING)
    public SendingStatus status;

    public MessageTemplate() {
    }

    public MessageTemplate(Person person, AttendanceList attendanceList) {
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
