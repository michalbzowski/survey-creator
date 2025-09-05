package pl.bzowski.tags;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    public Tag() {}

    public Tag(String name, UUID registeredUserId) {
        this.name = name;
        this.registeredUserId = registeredUserId;
    }

}
