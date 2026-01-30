package com.crcs.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Centralized Kafka Event Producer (Spring Boot wrapper)
 * Provides methods to send events to Kafka topics
 */
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducer.class);
    
    private final KafkaTemplate<String, String> notificationKafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send JSON message to Kafka topic
     * @param topic Topic name
     * @param key Message key
     * @param message JSON message to send
     */
    public void sendJsonMessage(String topic, String key, String message) {
        try {
            notificationKafkaTemplate.send(topic, key, message);
            log.debug("JSON message sent to topic: {} with key: {}", topic, key);
        } catch (Exception e) {
            log.error("Error sending JSON message to topic: {} with key: {}", topic, key, e);
            throw new RuntimeException("Failed to send JSON message", e);
        }
    }

    /**
     * Send JSON message to Kafka topic without key
     * @param topic Topic name
     * @param message JSON message to send
     */
    public void sendJsonMessage(String topic, String message) {
        sendJsonMessage(topic, null, message);
    }

    /**
     * Send KafkaEvent as JSON to topic
     * @param topic Topic name
     * @param event KafkaEvent to send
     */
    public void sendEvent(String topic, com.crcs.common.dto.notification.KafkaEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            sendJsonMessage(topic, event.getEventId(), eventJson);
            log.info("Kafka event sent to topic: {} with eventId: {}", topic, event.getEventId());
        } catch (Exception e) {
            log.error("Error sending Kafka event to topic: {}", topic, e);
            throw new RuntimeException("Failed to send Kafka event", e);
        }
    }
}
