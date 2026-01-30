package com.crcs.notification.email;

import com.crcs.common.dto.notification.Notification;

import java.util.ArrayList;
import java.util.List;

public class EmailTemplateBookingRequestSubmitted implements EmailTemplateInterface {
    private String subject;
    private String textBody;
    private String htmlBody;
    private List<String> emailTo;
    private List<String> emailCc;
    private List<String> emailBcc;
    private String emailFrom;
    private List<String> attachments;
    private String customerName;
    private String resourceName;

    @Override
    public void setNotification(Notification notification) {
        this.customerName = notification.getCommunicationParams().getContactName();
        if (this.customerName == null || this.customerName.isEmpty()) {
            this.customerName = "Valued User";
        }

        this.resourceName = notification.getData().getMetadata() != null
                ? notification.getData().getMetadata().getOrDefault("resourceName", "Resource")
                : "Resource";

        this.emailFrom = notification.getCommunicationParams().getFromEmail();
        if (this.emailFrom == null || this.emailFrom.isEmpty()) {
            this.emailFrom = DEFAULT_EMAIL_FROM;
        }

        List<String> toList = notification.getCommunicationParams().getToEmailList();
        this.emailTo = toList != null ? new ArrayList<>(toList) : new ArrayList<>();
        this.emailCc = new ArrayList<>();
        this.emailBcc = new ArrayList<>();
        this.attachments = new ArrayList<>();

        this.subject = "Booking Request Submitted - " + resourceName;
        this.textBody = buildTextBody();
        this.htmlBody = buildHtmlBody();
    }

    private String buildTextBody() {
        return String.format(
            "Hello %s,\n\n" +
            "Your booking request for %s has been submitted and is pending approval by a facility manager.\n\n" +
            "You will receive a confirmation email once your booking is approved.\n\n" +
            "Best regards,\n" +
            "The CRCS Team",
            customerName, resourceName
        );
    }

    private String buildHtmlBody() {
        return String.format(
            "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }" +
            ".content { padding: 20px; background-color: #f9f9f9; }" +
            "</style></head><body>" +
            "<div class=\"container\">" +
            "<div class=\"header\"><h1>Booking Request Submitted</h1></div>" +
            "<div class=\"content\">" +
            "<p>Hello %s,</p>" +
            "<p>Your booking request for <strong>%s</strong> has been submitted and is <strong>pending approval</strong> by a facility manager.</p>" +
            "<p>You will receive a confirmation email once your booking is approved.</p>" +
            "<p>Best regards,<br>The CRCS Team</p>" +
            "</div></div></body></html>",
            customerName, resourceName
        );
    }

    @Override
    public String getEmailSubject() { return subject; }
    @Override
    public String getEmailTextBody() { return textBody; }
    @Override
    public String getEmailHtmlBody() { return htmlBody; }
    @Override
    public List<String> getAttachments() { return attachments; }
    @Override
    public List<String> getEmailTo() { return emailTo; }
    @Override
    public List<String> getEmailCc() { return emailCc; }
    @Override
    public List<String> getEmailBcc() { return emailBcc; }
    @Override
    public String getEmailFrom() { return emailFrom; }
}
