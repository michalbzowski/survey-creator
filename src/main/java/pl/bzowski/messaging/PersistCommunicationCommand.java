package pl.bzowski.messaging;

import pl.bzowski.persons.Person;

import java.util.Map;
import java.util.UUID;

public class PersistCommunicationCommand {

    private final Channel channel;

    private final CommunicationTemplate communicationTemplate;

    private final UUID currentUserId;

    private final UUID personId;

    private final String personFirstName;

    private final String personLastName;

    private final String personEmail;

    private final SendingStatus status;

    private final Map<String, Object> properties;


    public PersistCommunicationCommand(Channel channel, CommunicationTemplate communicationTemplate, UUID currentUserId, Person person, Map<String, Object> properties) {
        this.channel = channel;
        this.communicationTemplate = communicationTemplate;
        this.currentUserId = currentUserId;
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.status = SendingStatus.TO_SEND;
        this.properties = properties;
    }

    public Channel getChannel() {
        return channel;
    }

    public CommunicationTemplate getCommunicationTemplate() {
        return communicationTemplate;
    }

    public UUID getCurrentUserId() {
        return currentUserId;
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

    public SendingStatus getStatus() {
        return status;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "CommunicationDto{" +
                "channel=" + channel +
                ", communicationTemplate=" + communicationTemplate +
                ", personId=" + personId +
                ", personFirstName='" + personFirstName + '\'' +
                ", personLastName='" + personLastName + '\'' +
                ", personEmail='" + personEmail + '\'' +
                ", status=" + status +
                ", properties=" + properties +
                '}';
    }
}
