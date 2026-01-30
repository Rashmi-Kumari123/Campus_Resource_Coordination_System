package com.crcs.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common Kafka Event DTO for notification events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaEvent {
    private String eventId;
    private String eventType;
    private String eventMessage;
    private long eventTime;
    private String responseTopic;
    private String userId;
    private String entityId;

    public KafkaEvent(String eventId, String eventType, String eventMessage) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventMessage = eventMessage;
        this.eventTime = System.currentTimeMillis();
    }
}
