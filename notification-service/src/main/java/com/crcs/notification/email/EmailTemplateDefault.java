package com.crcs.notification.email;

import com.crcs.common.dto.notification.Notification;

import java.util.ArrayList;
import java.util.List;

public class EmailTemplateDefault implements EmailTemplateInterface {
    private String subject = "Notification from CRCS";
    private String textBody = "You have a new notification from CRCS.";
    private String htmlBody = "<html><body><p>You have a new notification from CRCS.</p></body></html>";
    private List<String> emailTo = new ArrayList<>();
    private List<String> emailCc = new ArrayList<>();
    private List<String> emailBcc = new ArrayList<>();
    private String emailFrom = DEFAULT_EMAIL_FROM;
    private List<String> attachments = new ArrayList<>();

    @Override
    public void setNotification(Notification notification) {
        this.emailFrom = notification.getCommunicationParams().getFromEmail();
        if (this.emailFrom == null || this.emailFrom.isEmpty()) {
            this.emailFrom = DEFAULT_EMAIL_FROM;
        }
        this.emailTo = new ArrayList<>(notification.getCommunicationParams().getToEmailList());
        this.emailCc = notification.getCommunicationParams().getCcList() != null 
                ? new ArrayList<>(notification.getCommunicationParams().getCcList())
                : new ArrayList<>();
        this.emailBcc = notification.getCommunicationParams().getBccList() != null
                ? new ArrayList<>(notification.getCommunicationParams().getBccList())
                : new ArrayList<>();
        this.attachments = notification.getData().getAttachmentUris() != null
                ? new ArrayList<>(notification.getData().getAttachmentUris())
                : new ArrayList<>();
    }

    @Override
    public String getEmailSubject() {
        return subject;
    }

    @Override
    public String getEmailTextBody() {
        return textBody;
    }

    @Override
    public String getEmailHtmlBody() {
        return htmlBody;
    }

    @Override
    public List<String> getAttachments() {
        return attachments;
    }

    @Override
    public List<String> getEmailTo() {
        return emailTo;
    }

    @Override
    public List<String> getEmailCc() {
        return emailCc;
    }

    @Override
    public List<String> getEmailBcc() {
        return emailBcc;
    }

    @Override
    public String getEmailFrom() {
        return emailFrom;
    }
}
