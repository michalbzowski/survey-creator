package pl.bzowski.messaging.email;

import java.util.UUID;

public record EmailAttendanceEntryLinkSentEvent(UUID communicationId, UUID attendanceEntryId) {
}
