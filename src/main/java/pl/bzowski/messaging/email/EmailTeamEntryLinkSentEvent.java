package pl.bzowski.messaging.email;

import java.util.UUID;

public record EmailTeamEntryLinkSentEvent(UUID communicationId, UUID teamEntryId) {
}
