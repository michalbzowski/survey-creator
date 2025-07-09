package pl.bzowski.tags;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
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

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }

}
