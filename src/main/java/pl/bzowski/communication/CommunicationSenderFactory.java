package pl.bzowski.communication;

import jakarta.inject.Singleton;

@Singleton
public class CommunicationSenderFactory {


    private final EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender;

    public CommunicationSenderFactory(EmailNewPersonAddedCommunicationSender emailNewPersonAddedCommunicationSender) {
        this.emailNewPersonAddedCommunicationSender = emailNewPersonAddedCommunicationSender;
    }

    public CommunicationSender create(CommunicationTemplate communicationTemplate) {
        if (CommunicationTemplate.EMAIL_NEW_PERSON_ADDED.equals(communicationTemplate)) {
            return emailNewPersonAddedCommunicationSender;
        }
        return null;
    }
}
