package pl.bzowski.messaging;

import jakarta.inject.Singleton;
import pl.bzowski.messaging.email.EmailAttendanceRecordLinkSender;
import pl.bzowski.messaging.email.EmailNewPersonAddedCommunicationSender;

@Singleton
public class CommunicationSenderFactory {

    private final EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender;
    private final EmailAttendanceRecordLinkSender emailAttendanceRecordLinkSender;

    public CommunicationSenderFactory(EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender, EmailAttendanceRecordLinkSender emailAttendanceRecordLinkSender) {
        this.emailNewPersonAddedCommunicationSender = emailNewPersonAddedCommunicationSender;
        this.emailAttendanceRecordLinkSender = emailAttendanceRecordLinkSender;
    }

    public CommunicationSender create(CommunicationTemplate communicationTemplate) {
        if (CommunicationTemplate.EMAIL_NEW_PERSON_ADDED.equals(communicationTemplate)) {
            return emailNewPersonAddedCommunicationSender;
        } else if (CommunicationTemplate.ATTENDANCE_RECORD_LINK.equals(communicationTemplate)) {
            return emailAttendanceRecordLinkSender;
        }
        return null;
    }
}
