package pl.bzowski.team.list.api;


import java.util.List;
import java.util.UUID;

public class TeamDTO {

    public UUID id;
    public String name;
    public List<UUID> events;

    public TeamDTO() {
    }

    public TeamDTO(UUID eventId) {
        this.events = List.of(eventId);
    }

    public TeamDTO(UUID id, String name, List<UUID> events) {
        this.id = id;
        this.name = name;
        this.events = events;
    }
}
