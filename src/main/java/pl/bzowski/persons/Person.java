package pl.bzowski.persons;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.tags.Tag;

import java.util.UUID;

@Entity
@Table(name = "persons")
public class Person extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false, unique = true)
    public String email; //todo: Opracuj sposób na przypadki, gdy osoby są niepełnoletnie i maila wysyłasz zarówno do nich jak i do rodzicó

    @ManyToOne
    @JoinColumn(name = "tag_id")
    public Tag defaultTag;

    // Konstruktor domyślny wymagany przez JPA
    public Person() {}

    public Person(String firstName, String lastName, String email,  Tag defaultTag) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.defaultTag = defaultTag;
    }
}
