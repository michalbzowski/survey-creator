package pl.bzowski.communication;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

import static pl.bzowski.configurations.Configurations.MESSENGER;

@Entity
@Table(name = "communication_person_agreement")
public class CommunicationPersonAgreement extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    @Column(nullable = false)
    public UUID personId;

    @Column(nullable = false)
    public String personEmail;

    @Enumerated(EnumType.STRING)
    public Channel channel;

    @Column(nullable = false)
    public boolean agree = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> properties;


    public void confirm() {
        agree = true;
    }
}
