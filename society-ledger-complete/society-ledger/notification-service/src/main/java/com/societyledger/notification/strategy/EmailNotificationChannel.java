package com.societyledger.notification.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.enabled:true}")
    private boolean enabled;

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    @Async
    public void send(NotificationPayload payload) {
        if (payload.getRecipientEmail() == null || payload.getRecipientEmail().isBlank()) {
            log.warn("Email notification skipped: no recipient email for event {}", payload.getEventType());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(payload.getRecipientEmail());
            message.setSubject(payload.getSubject());
            message.setText(payload.getBody());
            mailSender.send(message);
            log.info("Email notification sent to {} for event {}", payload.getRecipientEmail(),
                    payload.getEventType());
        } catch (Exception e) {
            log.error("Failed to send email to {} for event {}: {}",
                    payload.getRecipientEmail(), payload.getEventType(), e.getMessage());
        }
    }
}
