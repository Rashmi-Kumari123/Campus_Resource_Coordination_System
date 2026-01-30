package com.crcs.notification.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class EmailSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    private final Session session;

    public EmailSender(@Value("${smtp.host:smtp.gmail.com}") String smtpHost,
                       @Value("${smtp.port:587}") String smtpPort,
                       @Value("${smtp.username:}") String username,
                       @Value("${smtp.password:}") String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", smtpHost);

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            logger.warn("SMTP credentials not found, using unauthenticated session");
            session = Session.getInstance(props);
        }
    }

    public boolean sendEmail(String from, List<String> to, List<String> cc, List<String> bcc,
                            String subject, String htmlBody, String textBody, List<String> attachments) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            
            // Set recipients
            for (String email : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }
            
            if (cc != null && !cc.isEmpty()) {
                for (String email : cc) {
                    if (StringUtils.isNotBlank(email)) {
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(email));
                    }
                }
            }
            
            if (bcc != null && !bcc.isEmpty()) {
                for (String email : bcc) {
                    if (StringUtils.isNotBlank(email)) {
                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
                    }
                }
            }
            
            message.setSubject(subject);
            
            // Create multipart message
            MimeMultipart multipart = new MimeMultipart("alternative");
            
            // Text part
            if (StringUtils.isNotBlank(textBody)) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody, "utf-8");
                multipart.addBodyPart(textPart);
            }
            
            // HTML part
            if (StringUtils.isNotBlank(htmlBody)) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);
            }
            
            // Attachments
            if (attachments != null && !attachments.isEmpty()) {
                for (String attachment : attachments) {
                    logger.info("Attachment URL: {}", attachment);
                }
            }
            
            message.setContent(multipart);
            
            // Send message
            Transport.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
        } catch (MessagingException e) {
            logger.error("Error sending email", e);
            return false;
        }
    }
}
