package com.crcs.common.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Common Notification Communication Parameters DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCommunicationParams {
    private NotificationChannel channel;
    private String fromEmail;
    @JsonProperty("toEmailList")
    private List<String> toEmailList;
    private List<String> ccList;
    private List<String> bccList;
    private String contactName;
    private String toPhone;

    public NotificationCommunicationParams(String channel, List<String> toEmailList, String contactName) {
        this.channel = channel != null ? NotificationChannel.valueOf(channel.toUpperCase()) : NotificationChannel.EMAIL;
        this.toEmailList = toEmailList;
        this.contactName = contactName;
    }
    
    public NotificationCommunicationParams(NotificationChannel channel, List<String> toEmailList, String contactName) {
        this.channel = channel;
        this.toEmailList = toEmailList;
        this.contactName = contactName;
    }
}
