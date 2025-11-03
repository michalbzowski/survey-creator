package pl.bzowski.members;


import java.util.List;
import java.util.UUID;


public record TeamCreatedDto(String withTeam, String teamType, List<UUID> groupIds, List<UUID> personIds, UUID teamId,
                             UUID registeredUserId) {

    public static final String EVENT_CREATED = "EVENT_CREATED";

    @Override
    public String toString() {
        return "TeamCreatedDto{" +
                "withTeam='" + withTeam + '\'' +
                ", teamType='" + teamType + '\'' +
                ", groupIds=" + groupIds +
                ", personIds=" + personIds +
                ", teamId=" + teamId +
                ", registeredUserId=" + registeredUserId +
                '}';
    }
}
