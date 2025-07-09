package pl.bzowski;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "queries")
public class Query extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String description;

    // Konstruktor domyślny wymagany przez JPA
    public Query() {
    }

    public Query(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
