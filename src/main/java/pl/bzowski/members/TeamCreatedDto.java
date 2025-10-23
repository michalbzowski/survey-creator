package pl.bzowski.members;


import java.util.List;
import java.util.UUID;


public class TeamCreatedDto {

    public static final String EVENT_CREATED = "EVENT_CREATED";

    private final String withTeam;
    private final String teamType;
    private final List<UUID> groupIds;
    private final List<UUID> personIds;
    private final UUID teamId;
    private final UUID registeredUserId;

    public TeamCreatedDto(String withTeam, String teamType, List<UUID> groupIds, List<UUID> personIds, UUID teamId, UUID registeredUserId) {
        this.withTeam = withTeam;
        this.teamType = teamType;
        this.groupIds = groupIds;
        this.personIds = personIds;
        this.teamId = teamId;
        this.registeredUserId = registeredUserId;
    }

    public String getWithTeam() {
        return withTeam;
    }

    public String getTeamType() {
        return teamType;
    }

    public List<UUID> getGroupIds() {
        return groupIds;
    }

    public List<UUID> getPersonIds() {
        return personIds;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getRegisteredUserId() {
        return registeredUserId;
    }
}
