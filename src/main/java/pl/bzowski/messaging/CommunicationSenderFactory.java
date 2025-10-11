package pl.bzowski.messaging;

import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.EmailTeamEntryLinkSender;
import pl.bzowski.messaging.email.EmailNewPersonAddedCommunicationSender;

@Singleton
public class CommunicationSenderFactory {

    private final EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender;
    private final EmailTeamEntryLinkSender emailTeamEntryLinkSender;

    public CommunicationSenderFactory(EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender, EmailTeamEntryLinkSender emailTeamEntryLinkSender) {
        this.emailNewPersonAddedCommunicationSender = emailNewPersonAddedCommunicationSender;
        this.emailTeamEntryLinkSender = emailTeamEntryLinkSender;
    }

    public CommunicationSender create(CommunicationTemplate communicationTemplate) {
        if (CommunicationTemplate.EMAIL_NEW_PERSON_ADDED.equals(communicationTemplate)) {
            return emailNewPersonAddedCommunicationSender;
        } else if (CommunicationTemplate.TEAM_RECORD_LINK.equals(communicationTemplate)) {
            return emailTeamEntryLinkSender;
        }
        return null;
    }
}
