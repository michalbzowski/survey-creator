package pl.bzowski.members;

import java.util.UUID;

public record MembersAssignedDto(UUID registeredUserId, UUID teamId) {

    public static final String MEMBERS_ASSIGNED = "MEMBERS_ASSIGNED";

    @Override
    public String toString() {
        return "MembersAssignedDto{" +
                "registeredUserId=" + registeredUserId +
                ", teamId=" + teamId +
                '}';
    }
}
