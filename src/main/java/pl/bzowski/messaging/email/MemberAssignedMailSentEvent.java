package pl.bzowski.messaging.email;

import java.util.UUID;

public record MemberAssignedMailSentEvent(UUID communicationId, UUID teamEntryId) {
}
