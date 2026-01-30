package com.crcs.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common Notification DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private NotificationData data;
    private NotificationCommunicationParams communicationParams;
    private boolean createPDF;

    public Notification(NotificationData data, NotificationCommunicationParams communicationParams) {
        this.data = data;
        this.communicationParams = communicationParams;
        this.createPDF = false;
    }
}
