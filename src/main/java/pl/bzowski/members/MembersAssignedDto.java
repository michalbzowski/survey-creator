package pl.bzowski.members;

import java.util.UUID;

public class MembersAssignedDto {

    public static final String MEMBERS_ASSIGNED = "MEMBERS_ASSIGNED";

    private UUID registeredUserId;
    private UUID teamId;

    public MembersAssignedDto(UUID registeredUserId, UUID teamId) {
        this.registeredUserId = registeredUserId;
        this.teamId = teamId;
    }

    public UUID getRegisteredUserId() {
        return registeredUserId;
    }

    public UUID getTeamId() {
        return teamId;
    }
}
