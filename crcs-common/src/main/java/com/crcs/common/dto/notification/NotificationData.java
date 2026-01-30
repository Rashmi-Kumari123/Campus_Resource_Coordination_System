package com.crcs.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Common Notification Data DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationData {
    private String templateId;
    private Map<String, String> metadata;
    private String customerEmail;
    private String customerName;
    private List<String> attachmentUris;

    public NotificationData(String templateId, String customerEmail, String customerName) {
        this.templateId = templateId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
    }
}
