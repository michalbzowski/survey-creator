package pl.bzowski.surveys;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.ws.rs.FormParam;

import java.util.UUID;

@Entity
@Table(name = "surveys")
public class Survey extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    @FormParam("title")
    public String title;

    @Column(nullable = false)
    @FormParam("description")
    public String description;

    // Konstruktor domyślny wymagany przez JPA
    public Survey() {
    }

    public Survey(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
