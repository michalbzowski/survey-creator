package pl.bzowski.tags;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.security.RegisteredUser;

import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @ManyToOne
    @JoinColumn(name = "registered_user_id")
    public RegisteredUser registeredUser;

    public Tag() {}

    public Tag(String name, RegisteredUser registeredUser) {
        this.name = name;
        this.registeredUser = registeredUser;
    }

}
