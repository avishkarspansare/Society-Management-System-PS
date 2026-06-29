package com.societyledger.notification.service;

import com.societyledger.notification.strategy.NotificationChannel;
import com.societyledger.notification.strategy.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final List<NotificationChannel> channels;

    /**
     * Dispatch a notification to a specific recipient through all enabled channels.
     */
    public void dispatch(NotificationPayload payload) {
        channels.stream()
                .filter(NotificationChannel::isEnabled)
                .forEach(channel -> {
                    try {
                        channel.send(payload);
                    } catch (Exception e) {
                        log.error("Channel {} failed to send notification for event {}: {}",
                                channel.getChannelName(), payload.getEventType(), e.getMessage());
                    }
                });
    }

    /**
     * Broadcast to all residents in a society.
     * In production this would look up resident emails from society-service.
     * For now we log — full implementation would use a Feign call.
     */
    public void notifyAllResidents(Long societyId, String subject, String body, String eventType) {
        log.info("Broadcasting {} to all residents of society {}: {}",
                eventType, societyId, subject);
        // TODO: Call society-service to get all resident emails, then dispatch to each
        // This is a future enhancement — current implementation logs the intent
    }
}
