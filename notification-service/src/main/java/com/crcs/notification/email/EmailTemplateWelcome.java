package com.crcs.notification.email;

import com.crcs.common.dto.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EmailTemplateWelcome implements EmailTemplateInterface {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateWelcome.class);
    
    private String subject;
    private String textBody;
    private String htmlBody;
    private List<String> emailTo;
    private List<String> emailCc;
    private List<String> emailBcc;
    private String emailFrom;
    private List<String> attachments;
    private String customerName;

    @Override
    public void setNotification(Notification notification) {
        this.customerName = notification.getCommunicationParams().getContactName();
        if (this.customerName == null || this.customerName.isEmpty()) {
            this.customerName = "Valued User";
        }
        
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
        
        this.subject = "Welcome to Campus Resource Coordination System!";
        this.textBody = buildTextBody();
        this.htmlBody = buildHtmlBody();
    }

    private String buildTextBody() {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to Campus Resource Coordination System (CRCS)! We're excited to have you on board.\n\n" +
            "CRCS helps you:\n" +
            "- Book campus resources (rooms, labs, equipment)\n" +
            "- Track resource availability in real-time\n" +
            "- Manage your bookings efficiently\n\n" +
            "If you have any questions, feel free to reach out to our support team.\n\n" +
            "Best regards,\n" +
            "The CRCS Team",
            customerName
        );
    }

    private String buildHtmlBody() {
        return String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
            "        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }\n" +
            "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
            "        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>Welcome to CRCS!</h1>\n" +
            "        </div>\n" +
            "        <div class=\"content\">\n" +
            "            <p>Hello %s,</p>\n" +
            "            <p>Welcome to Campus Resource Coordination System (CRCS)! We're excited to have you on board.</p>\n" +
            "            <p>CRCS helps you:</p>\n" +
            "            <ul>\n" +
            "                <li>Book campus resources (rooms, labs, equipment)</li>\n" +
            "                <li>Track resource availability in real-time</li>\n" +
            "                <li>Manage your bookings efficiently</li>\n" +
            "            </ul>\n" +
            "            <p>If you have any questions, feel free to reach out to our support team.</p>\n" +
            "            <p>Best regards,<br>The CRCS Team</p>\n" +
            "        </div>\n" +
            "        <div class=\"footer\">\n" +
            "            <p>&copy; 2025 CRCS. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            customerName
        );
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
