package pl.bzowski.members;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.groups.Group;
import pl.bzowski.persons.Person;
import pl.bzowski.team.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "members")
public class Member extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column
    public UUID personId;

    @Column
    public String personFirstName;

    @Column
    public String personLastName;

    @Column
    public String personEmail;

    @Column(nullable = false)
    public UUID teamId;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Column(nullable = false)
    public Boolean teamAnswered = false;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinTable(
            name = "team_member",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    public Team team;

    @Column
    public String personTag;

    Member() {

    }

    public Member(Person person, Team team) {
        this.team = team;
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.teamId = team.id;
        this.linkToken = UUID.randomUUID();
    }
}
