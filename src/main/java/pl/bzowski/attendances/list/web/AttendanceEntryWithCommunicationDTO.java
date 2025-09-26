package pl.bzowski.attendances.list.web;

import java.util.UUID;

public class AttendanceEntryWithCommunicationDTO {

    public UUID id;
    public UUID personId;
    public String personFirstName;
    public String personLastName;
    public String personEmail;
    public UUID attendanceListId;
    public UUID linkToken;
    public Boolean communicationSent; // dodatkowe pole z left join
    public Boolean attendanceListAnswered;

    public AttendanceEntryWithCommunicationDTO(UUID id, UUID personId, String personFirstName, String personLastName, String personEmail, UUID attendanceListId, UUID linkToken,  Boolean attendanceListAnswered, Boolean communicationSent) {
        this.id = id;
        this.personId = personId;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personEmail = personEmail;
        this.attendanceListId = attendanceListId;
        this.linkToken = linkToken;
        this.attendanceListAnswered = attendanceListAnswered;
        this.communicationSent = communicationSent;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getPersonFirstName() {
        return personFirstName;
    }

    public String getPersonLastName() {
        return personLastName;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public UUID getAttendanceListId() {
        return attendanceListId;
    }

    public UUID getLinkToken() {
        return linkToken;
    }

    public Boolean getAttendanceListAnswered() {
        return attendanceListAnswered;
    }

    public Boolean getCommunicationSent() {
        return communicationSent;
    }
}
