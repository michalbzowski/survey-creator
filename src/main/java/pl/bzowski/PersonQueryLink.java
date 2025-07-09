package pl.bzowski;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "person_query_links")
public class PersonQueryLink extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(optional = false)
    public Person person;

    @ManyToOne(optional = false)
    public Query query;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Enumerated(EnumType.STRING)
    public Answer answer;

    public enum Answer {
        YES, NO
    }

    public PersonQueryLink() {
    }

    public PersonQueryLink(Person person, Query query) {
        this.person = person;
        this.query = query;
        this.linkToken = UUID.randomUUID();
    }
}
