package pl.bzowski.integrations;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "integrations")
public class Integrations extends PanacheEntityBase {

    public static final String MESSENGER = "messengerRegistrationKey";

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> configuration;
}
