package pl.bzowski.persons;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.group.Group;
import pl.bzowski.tags.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "persons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email", "registered_user_id"}))
public class Person extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false)
    public String email; //todo: Opracuj sposób na przypadki, gdy osoby są niepełnoletnie i maila wysyłasz zarówno do nich jak i do rodzicó

    @ManyToOne
    @JoinColumn(name = "tag_id")
    public Tag defaultTag;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    @ManyToMany
    @JoinTable(
            name = "person_group",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    public Set<Group> groups = new HashSet<>();

    // Konstruktor domyślny wymagany przez JPA
    public Person() {}

    public Person(String firstName, String lastName, String email, Tag defaultTag) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.defaultTag = defaultTag;
    }

    public boolean isInGroup(Group group) {
        return this.groups.stream().anyMatch(g -> g.id.equals(group.id));
    }
}
