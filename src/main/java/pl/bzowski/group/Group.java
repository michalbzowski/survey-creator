package pl.bzowski.group;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.persons.Person;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class Group extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(name = "registered_user_id", nullable = false)
    public UUID registeredUserId;

    @ManyToMany(mappedBy = "groups")
    public Set<Person> members;

    public Group() {}

    public Group(String name, UUID registeredUserId) {
        this.name = name;
        this.registeredUserId = registeredUserId;
    }

    public boolean hasThat(Person person) {
        return this.members != null && this.members.stream().anyMatch(m -> m.id.equals(person.id));
    }
}
