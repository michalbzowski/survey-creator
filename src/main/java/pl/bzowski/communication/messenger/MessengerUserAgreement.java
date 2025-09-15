package pl.bzowski.communication.messenger;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

import static pl.bzowski.integrations.Integrations.MESSENGER;

@Entity
@Table(name = "messenger_user_agreement")
public class MessengerUserAgreement extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String psid;

    @Column(nullable = false)
    public String email;

    @Column(name = MESSENGER, nullable = false)
    public UUID messengerRegistrationKey;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    @Column(nullable = false)
    public boolean agree;
}
