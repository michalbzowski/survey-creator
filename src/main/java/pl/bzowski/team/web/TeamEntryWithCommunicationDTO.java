package pl.bzowski.team.web;

import java.util.UUID;

public class TeamEntryWithCommunicationDTO {

    public UUID id;
    public UUID personId;
    public String personFirstName;
    public String personLastName;
    public String personEmail;
    public UUID teamId;
    public UUID linkToken;
    public Boolean communicationSent; // dodatkowe pole z left join
    public Boolean teamAnswered;

    public TeamEntryWithCommunicationDTO(UUID id, UUID personId, String personFirstName, String personLastName, String personEmail, UUID teamId, UUID linkToken,  Boolean teamAnswered, Boolean communicationSent) {
        this.id = id;
        this.personId = personId;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personEmail = personEmail;
        this.teamId = teamId;
        this.linkToken = linkToken;
        this.teamAnswered = teamAnswered;
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

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getLinkToken() {
        return linkToken;
    }

    public Boolean getTeamAnswered() {
        return teamAnswered;
    }

    public Boolean getCommunicationSent() {
        return communicationSent;
    }
}
