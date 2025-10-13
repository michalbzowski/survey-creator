package pl.bzowski.answers;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import pl.bzowski.events.Event;
import pl.bzowski.members.Member;
import pl.bzowski.team.Team;

import java.util.UUID;

@Entity
@Table(name = "answers")
public class Answer extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    public Member member;

    @ManyToOne
    @JoinColumn(name = "team_id")
    public Team team;

    @ManyToOne
    @JoinColumn(name = "event_id")
    public Event event;

    @Enumerated(EnumType.STRING)
    public AnswerValue answerValue;

    public enum AnswerValue {
        TAK, NIE, ODPOWIEM_POZNIEJ
    }
}
