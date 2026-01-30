package com.crcs.notification;

import com.crcs.notification.email.EmailSender;
import com.crcs.notification.email.EmailTemplateInterface;
import com.crcs.notification.email.EmailTemplateRegistry;
import com.crcs.common.dto.notification.KafkaEvent;
import com.crcs.common.dto.notification.Notification;
import com.crcs.notification.model.NotificationResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    /** Placeholder/invalid email domains - never send to these (e.g. from API docs). */
    private static final Set<String> PLACEHOLDER_EMAIL_DOMAINS = Set.of("example.com", "example.org");
    private final EmailSender emailSender;
    private final EmailTemplateRegistry templateRegistry;
    private final Set<String> processedNotifications = new HashSet<>();

    public NotificationHandler(EmailSender emailSender, EmailTemplateRegistry templateRegistry) {
        this.emailSender = emailSender;
        this.templateRegistry = templateRegistry;
    }

    public void processNotification(Notification notification, KafkaEvent event) {
        logger.info("Processing notification: {}", notification);
        createNotification(notification);
    }

    public NotificationResponse createNotification(Notification notification) {
        try {
            logger.info("Creating notification: {}", notification);

            // Sanitize recipients: never send to placeholder emails (e.g. user@example.com)
            if (notification.getCommunicationParams() != null && notification.getCommunicationParams().getToEmailList() != null) {
                List<String> original = notification.getCommunicationParams().getToEmailList();
                List<String> validRecipients = original.stream()
                        .filter(email -> email != null && !email.isBlank() && !isPlaceholderEmail(email))
                        .collect(Collectors.toList());
                if (validRecipients.size() != original.size()) {
                    logger.warn("Filtered out {} placeholder/invalid recipient(s), proceeding with {} valid", original.size() - validRecipients.size(), validRecipients.size());
                }
                notification.getCommunicationParams().setToEmailList(validRecipients);
            }
            
            // Check for duplicate notifications
            String notificationHash = generateNotificationHash(notification);
            synchronized (processedNotifications) {
                if (processedNotifications.contains(notificationHash)) {
                    logger.info("Duplicate notification detected, ignoring: {}", notificationHash);
                    return NotificationResponse.builder()
                            .httpCode("200")
                            .responseMessage("Duplicate notification request")
                            .build();
                }
                processedNotifications.add(notificationHash);
                // Keep only last 10000 hashes to prevent memory issues
                if (processedNotifications.size() > 10000) {
                    processedNotifications.clear();
                }
            }
            
            // Get email template
            String templateId = notification.getData().getTemplateId();
            EmailTemplateInterface emailTemplate = templateRegistry.getTemplate(templateId);
            
            if (emailTemplate == null) {
                logger.warn("No template found for templateId: {}", templateId);
                return NotificationResponse.builder()
                        .httpCode("400")
                        .responseMessage(StringUtils.join("Template not found: ", templateId))
                        .build();
            }
            
            // Set notification data in template
            emailTemplate.setNotification(notification);
            
            // Skip sending if no valid recipients (e.g. placeholder email filtered out)
            if (emailTemplate.getEmailTo() == null || emailTemplate.getEmailTo().isEmpty()) {
                logger.warn("No valid recipients for notification template: {}, skipping send", templateId);
                return NotificationResponse.builder()
                        .httpCode("200")
                        .responseMessage("Skipped - no valid recipients")
                        .build();
            }
            
            // Send email
            boolean result = emailSender.sendEmail(
                    emailTemplate.getEmailFrom(),
                    emailTemplate.getEmailTo(),
                    emailTemplate.getEmailCc(),
                    emailTemplate.getEmailBcc(),
                    emailTemplate.getEmailSubject(),
                    emailTemplate.getEmailHtmlBody(),
                    emailTemplate.getEmailTextBody(),
                    emailTemplate.getAttachments()
            );
            
            if (result) {
                logger.info("Email sent successfully");
                return NotificationResponse.builder()
                        .httpCode("200")
                        .responseMessage("SUCCESS")
                        .build();
            } else {
                logger.error("Failed to send email");
                return NotificationResponse.builder()
                        .httpCode("400")
                        .responseMessage("FAILURE")
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error creating notification", e);
            return NotificationResponse.builder()
                    .httpCode("500")
                    .responseMessage(StringUtils.join("Error: ", StringUtils.defaultString(e.getMessage(), "Unknown error")))
                    .build();
        }
    }

    private static boolean isPlaceholderEmail(String email) {
        if (email == null || !email.contains("@")) return true;
        String domain = email.substring(email.indexOf('@') + 1).trim().toLowerCase();
        return PLACEHOLDER_EMAIL_DOMAINS.contains(domain);
    }

    private String generateNotificationHash(Notification notification) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String notificationStr = StringUtils.defaultString(notification.toString());
            byte[] hash = digest.digest(notificationStr.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error generating notification hash", e);
            return String.valueOf(notification.hashCode());
        }
    }
}
