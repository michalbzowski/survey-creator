package pl.bzowski.configurations;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "integrations")
public class Configurations extends PanacheEntityBase {

    public static final String EMAIL_FROM = "email_from";
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
