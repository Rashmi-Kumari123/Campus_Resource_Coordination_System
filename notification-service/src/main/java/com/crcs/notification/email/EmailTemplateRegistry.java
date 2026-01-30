package com.crcs.notification.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailTemplateRegistry {
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateRegistry.class);
    private final Map<String, EmailTemplateInterface> templates = new HashMap<>();

    public EmailTemplateRegistry() {
        // Register templates
        registerTemplate("WELCOME", new EmailTemplateWelcome());
        registerTemplate("BOOKING_CONFIRMED", new EmailTemplateBookingConfirmed());
        registerTemplate("BOOKING_REQUEST_SUBMITTED", new EmailTemplateBookingRequestSubmitted());
        registerTemplate("BOOKING_CANCELLED", new EmailTemplateBookingCancelled());
        registerTemplate("RESOURCE_AVAILABLE", new EmailTemplateResourceAvailable());
        logger.info("Email template registry initialized with {} templates", templates.size());
    }

    public void registerTemplate(String templateId, EmailTemplateInterface template) {
        templates.put(templateId, template);
        logger.debug("Registered email template: {}", templateId);
    }

    public EmailTemplateInterface getTemplate(String templateId) {
        EmailTemplateInterface template = templates.get(templateId);
        if (template == null) {
            logger.warn("Template not found: {}, using default template", templateId);
            return new EmailTemplateDefault();
        }
        return template;
    }
}
