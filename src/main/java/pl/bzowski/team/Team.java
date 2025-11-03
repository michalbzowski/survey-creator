package pl.bzowski.team;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.bzowski.events.Event;
import pl.bzowski.members.Member;
import pl.bzowski.team.api.TeamDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "teams")
public class Team extends PanacheEntityBase {

    private static final Logger log = LoggerFactory.getLogger(Team.class);
    @Id
    @GeneratedValue
    public UUID id;

    @Column
    public String name;

    @Column(nullable = false, name = "registered_user_id")
    public UUID registeredUserId;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "team_event",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    public List<Event> events;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "team_member",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    public Set<Member> members = new HashSet<>();

    public Team() {
    }

    public Team(String name, List<Event> events) {
        this.name = name;
        this.events = events;
    }

    public TeamDTO toDTO() {
        TeamDTO dto = new TeamDTO(this.id, this.name, this.events.stream().map(e -> e.id).toList());
        log.info("- toDTO: {}", dto);
        return dto;
    }

    public String joinedEventsName() {
        return events.stream().map(e -> e.name).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", registeredUserId=" + registeredUserId +
                ", events=" + events +
                ", members=" + members +
                '}';
    }
}
