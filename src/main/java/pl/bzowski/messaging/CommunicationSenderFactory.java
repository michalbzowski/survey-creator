package pl.bzowski.messaging;

import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.EmailAttendanceEntryLinkSender;
import pl.bzowski.messaging.email.EmailNewPersonAddedCommunicationSender;

@Singleton
public class CommunicationSenderFactory {

    private final EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender;
    private final EmailAttendanceEntryLinkSender emailAttendanceEntryLinkSender;

    public CommunicationSenderFactory(EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender, EmailAttendanceEntryLinkSender emailAttendanceEntryLinkSender) {
        this.emailNewPersonAddedCommunicationSender = emailNewPersonAddedCommunicationSender;
        this.emailAttendanceEntryLinkSender = emailAttendanceEntryLinkSender;
    }

    public CommunicationSender create(CommunicationTemplate communicationTemplate) {
        if (CommunicationTemplate.EMAIL_NEW_PERSON_ADDED.equals(communicationTemplate)) {
            return emailNewPersonAddedCommunicationSender;
        } else if (CommunicationTemplate.ATTENDANCE_RECORD_LINK.equals(communicationTemplate)) {
            return emailAttendanceEntryLinkSender;
        }
        return null;
    }
}
