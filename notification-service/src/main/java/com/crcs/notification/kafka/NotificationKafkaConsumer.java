package com.crcs.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.crcs.notification.NotificationHandler;
import com.crcs.common.dto.notification.KafkaEvent;
import com.crcs.common.dto.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationKafkaConsumer.class);
    
    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.notification.topic:crcs-notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotification(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            logger.info("Received notification message - Key: {}, Partition: {}, Offset: {}", key, partition, offset);
            
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);
            processEvent(event);
        } catch (Exception e) {
            logger.error("Error processing notification message", e);
            // In production, you might want to send to a dead letter queue
        }
    }

    private void processEvent(KafkaEvent event) {
        try {
            logger.info("Processing event: {}", event.getEventType());
            
            switch (event.getEventType()) {
                case "CREATE_NOTIFICATION":
                    Notification notification = objectMapper.readValue(
                            event.getEventMessage(), Notification.class);
                    notificationHandler.processNotification(notification, event);
                    break;
                    
                default:
                    logger.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing event", e);
        }
    }
}
