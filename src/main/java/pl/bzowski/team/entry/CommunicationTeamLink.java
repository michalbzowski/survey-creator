package pl.bzowski.team.entry;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "communication_team_links")
public class CommunicationTeamLink extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public UUID communicationId;

    @Column(nullable = false)
    public UUID teamEntryId;

    public CommunicationTeamLink() {
    }

    public CommunicationTeamLink(UUID communicationId, UUID teamEntryId) {
        this.communicationId = communicationId;
        this.teamEntryId = teamEntryId;
    }
}
