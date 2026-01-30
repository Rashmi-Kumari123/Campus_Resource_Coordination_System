package com.crcs.notification.email;

import com.crcs.common.dto.notification.Notification;

import java.util.List;

public interface EmailTemplateInterface {
    String DEFAULT_EMAIL_FROM = "no-reply@crcs.com";
    
    String getEmailSubject();
    String getEmailTextBody();
    String getEmailHtmlBody();
    List<String> getAttachments();
    List<String> getEmailTo();
    List<String> getEmailCc();
    List<String> getEmailBcc();
    String getEmailFrom();
    void setNotification(Notification notification);
}
