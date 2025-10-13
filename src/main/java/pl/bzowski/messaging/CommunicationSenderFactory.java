package pl.bzowski.messaging;

import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.MemberAssignedMailSender;
import pl.bzowski.messaging.email.EmailNewPersonAddedCommunicationSender;

@Singleton
public class CommunicationSenderFactory {

    private final EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender;
    private final MemberAssignedMailSender memberAssignedMailSender;

    public CommunicationSenderFactory(EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender, MemberAssignedMailSender memberAssignedMailSender) {
        this.emailNewPersonAddedCommunicationSender = emailNewPersonAddedCommunicationSender;
        this.memberAssignedMailSender = memberAssignedMailSender;
    }

    public CommunicationSender create(CommunicationTemplate communicationTemplate) {
        if (CommunicationTemplate.EMAIL_NEW_PERSON_ADDED.equals(communicationTemplate)) {
            return emailNewPersonAddedCommunicationSender;
        } else if (CommunicationTemplate.TEAM_RECORD_LINK.equals(communicationTemplate)) {
            return memberAssignedMailSender;
        }
        return null;
    }
}
