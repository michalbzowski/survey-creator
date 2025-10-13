package pl.bzowski.team.member;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.team.Team;
import pl.bzowski.persons.Person;

import java.util.UUID;

@Entity
@Table(name = "team_member")
public class TeamMember extends PanacheEntityBase {

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

    @Column
    public String personTag;

    @ManyToOne(optional = false)
    public Team team;

    @Column(nullable = false)
    public UUID teamId;

    @Column(nullable = false, unique = true)
    public UUID linkToken; // unikalny identyfikator do URL-a

    @Column(nullable = false)
    public Boolean teamAnswered = false;

    public TeamMember() {
    }

    public TeamMember(Person person, Team team) {
        this.personId = person.id;
        this.personFirstName = person.firstName;
        this.personLastName = person.lastName;
        this.personEmail = person.email;
        this.team = team;
        this.teamId = team.id;
        this.linkToken = UUID.randomUUID();
    }
}
