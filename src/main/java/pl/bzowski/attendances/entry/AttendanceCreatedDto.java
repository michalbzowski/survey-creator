package pl.bzowski.attendances.entry;

import pl.bzowski.events.web.EventDto;

import java.util.List;
import java.util.UUID;


public class AttendanceCreatedDto {

    public static final String EVENT_WITH_ATTENDANCE_CREATED = "GENERATE-LINKS-EVENT";

    private final String withAttendanceList;
    private final String attendanceType;
    private final List<UUID> groupIds;
    private final List<UUID> personIds;
    private final UUID attendanceListId;
    private final UUID registeredUserId;
    private final EventDto eventDto;

    public AttendanceCreatedDto(String withAttendanceList, String attendanceType, List<UUID> groupIds, List<UUID> personIds, UUID attendanceListId, UUID registeredUserId, EventDto eventDto) {
        this.withAttendanceList = withAttendanceList;
        this.attendanceType = attendanceType;
        this.groupIds = groupIds;
        this.personIds = personIds;
        this.attendanceListId = attendanceListId;
        this.registeredUserId = registeredUserId;
        this.eventDto = eventDto;
    }

    public String getWithAttendanceList() {
        return withAttendanceList;
    }

    public String getAttendanceType() {
        return attendanceType;
    }

    public List<UUID> getGroupIds() {
        return groupIds;
    }

    public List<UUID> getPersonIds() {
        return personIds;
    }

    public UUID getAttendanceListId() {
        return attendanceListId;
    }

    public UUID getRegisteredUserId() {
        return registeredUserId;
    }

    public EventDto getEventDto() {
        return eventDto;
    }
}
