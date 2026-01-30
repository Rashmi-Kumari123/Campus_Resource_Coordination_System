package com.crcs.kafka.producer;

import com.crcs.common.dto.notification.KafkaEvent;
import com.crcs.common.dto.notification.Notification;
import com.crcs.common.dto.notification.NotificationChannel;
import com.crcs.common.dto.notification.NotificationCommunicationParams;
import com.crcs.common.dto.notification.NotificationData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generic publisher for notification events (welcome, password reset, booking, etc.).
 * Any service can use this to send notifications via Kafka to the notification-service.
 */
@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);
    private static final String EVENT_TYPE_CREATE_NOTIFICATION = "CREATE_NOTIFICATION";

    /** Template IDs used by notification-service (EmailTemplateRegistry). */
    public static final String TEMPLATE_WELCOME = "WELCOME";
    public static final String TEMPLATE_BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String TEMPLATE_BOOKING_CANCELLED = "BOOKING_CANCELLED";
    public static final String TEMPLATE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TEMPLATE_RESOURCE_AVAILABLE = "RESOURCE_AVAILABLE";

    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.notification.topic:crcs-notification}")
    private String defaultNotificationTopic;

    public NotificationEventPublisher(KafkaEventProducer kafkaEventProducer, ObjectMapper objectMapper) {
        this.kafkaEventProducer = kafkaEventProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a notification event (generic: welcome, password reset, booking, etc.).
     *
     * @param templateId   Template ID (e.g. WELCOME, BOOKING_CONFIRMED, PASSWORD_RESET)
     * @param toEmails     Recipient email addresses
     * @param contactName  Display name for the recipient
     * @param userId       User ID (optional, can be null)
     * @param metadata     Optional key-value data for the template (can be null or empty)
     */
    public void publish(String templateId, List<String> toEmails, String contactName,
                       String userId, Map<String, String> metadata) {
        publish(templateId, toEmails, contactName, userId, metadata, defaultNotificationTopic);
    }

    /**
     * Publish a notification event to a specific topic.
     */
    public void publish(String templateId, List<String> toEmails, String contactName,
                       String userId, Map<String, String> metadata, String topic) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Cannot publish notification: no recipients for templateId={}", templateId);
            return;
        }
        try {
            String primaryEmail = toEmails.get(0);
            NotificationData data = new NotificationData(templateId, primaryEmail, contactName);
            data.setMetadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>());

            NotificationCommunicationParams params = new NotificationCommunicationParams(
                    NotificationChannel.EMAIL,
                    toEmails,
                    contactName != null ? contactName : "User"
            );

            Notification notification = new Notification(data, params);

            KafkaEvent event = new KafkaEvent(
                    UUID.randomUUID().toString(),
                    EVENT_TYPE_CREATE_NOTIFICATION,
                    objectMapper.writeValueAsString(notification)
            );
            if (userId != null) {
                event.setUserId(userId);
            }

            kafkaEventProducer.sendEvent(topic, event);
            log.info("Notification event published: templateId={}, recipients={}, userId={}", templateId, toEmails.size(), userId);
        } catch (Exception e) {
            log.error("Failed to publish notification: templateId={}, userId={}", templateId, userId, e);
            throw new RuntimeException("Failed to publish notification event", e);
        }
    }

    /**
     * Convenience: single recipient, no metadata.
     */
    public void publish(String templateId, String toEmail, String contactName, String userId) {
        publish(templateId, List.of(toEmail), contactName, userId, null);
    }

    /**
     * Convenience: single recipient with metadata.
     */
    public void publish(String templateId, String toEmail, String contactName, String userId, Map<String, String> metadata) {
        publish(templateId, List.of(toEmail), contactName, userId, metadata);
    }

    /**
     * Publish welcome email (e.g. after signup).
     */
    public void publishWelcome(String toEmail, String contactName, String userId) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot publish welcome: no email");
            return;
        }
        Map<String, String> meta = new HashMap<>();
        if (userId != null) meta.put("userId", userId);
        meta.put("email", toEmail);
        publish(TEMPLATE_WELCOME, toEmail, contactName != null ? contactName : "Valued User", userId, meta);
    }

    /**
     * Publish password reset notification (for future use).
     */
    public void publishPasswordReset(String toEmail, String contactName, String userId, String resetTokenOrLink) {
        Map<String, String> meta = new HashMap<>();
        if (userId != null) meta.put("userId", userId);
        if (resetTokenOrLink != null) meta.put("resetLink", resetTokenOrLink);
        publish(TEMPLATE_PASSWORD_RESET, toEmail, contactName, userId, meta);
    }

    /**
     * Publish booking confirmation (for booking-service).
     */
    public void publishBookingConfirmed(String toEmail, String contactName, String userId, String resourceName) {
        Map<String, String> meta = new HashMap<>();
        if (userId != null) meta.put("userId", userId);
        if (resourceName != null) meta.put("resourceName", resourceName);
        publish(TEMPLATE_BOOKING_CONFIRMED, toEmail, contactName, userId, meta);
    }

    /**
     * Publish booking cancelled (for booking-service).
     */
    public void publishBookingCancelled(String toEmail, String contactName, String userId, String resourceName) {
        Map<String, String> meta = new HashMap<>();
        if (userId != null) meta.put("userId", userId);
        if (resourceName != null) meta.put("resourceName", resourceName);
        publish(TEMPLATE_BOOKING_CANCELLED, toEmail, contactName, userId, meta);
    }
}
