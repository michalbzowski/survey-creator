package pl.bzowski.messaging;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "communications")
public class Communication  extends PanacheEntityBase {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    private CommunicationTemplate communicationTemplate;

    @Column(nullable = false)
    private UUID personId;

    @Column(nullable = false)
    private String personFirstName;

    @Column(nullable = false)
    private String personLastName;

    @Column(nullable = false)
    private String personEmail;

    @Enumerated(EnumType.STRING)
    private SendingStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> properties;

    public Communication() {
    }

    public Communication(Channel channel, CommunicationTemplate communicationTemplate, UUID personId, String personFirstName, String personLastName, String personEmail, SendingStatus status, Map<String, Object> properties) {
        this.id = null;
        this.channel = channel;
        this.communicationTemplate = communicationTemplate;
        this.personId = personId;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personEmail = personEmail;
        this.status = status;
        this.properties = properties;
    }

    public Communication(UUID id, Channel channel, CommunicationTemplate communicationTemplate, UUID personId, String personFirstName, String personLastName, String personEmail, SendingStatus status, Map<String, Object> properties) {
        this.id = id;
        this.channel = channel;
        this.communicationTemplate = communicationTemplate;
        this.personId = personId;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personEmail = personEmail;
        this.status = status;
        this.properties = properties;
    }

    public void statusSent() {
        this.status = SendingStatus.SENT;
    }

    public UUID getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public CommunicationTemplate getCommunicationTemplate() {
        return communicationTemplate;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getPersonFirstName() {
        return personFirstName;
    }

    public String getPersonLastName() {
        return personLastName;
    }
}
