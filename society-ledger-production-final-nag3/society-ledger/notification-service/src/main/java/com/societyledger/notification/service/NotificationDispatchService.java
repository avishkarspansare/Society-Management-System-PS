package com.societyledger.notification.service;

import com.societyledger.notification.entity.NotificationLog;
import com.societyledger.notification.repository.NotificationLogRepository;
import com.societyledger.notification.strategy.NotificationChannel;
import com.societyledger.notification.strategy.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final List<NotificationChannel> channels;
    private final NotificationLogRepository logRepository;

    /**
     * Dispatch a notification to a specific recipient through all enabled channels.
     * Every attempt — success or failure — is persisted to notification_log.
     */
    public void dispatch(NotificationPayload payload) {
        channels.stream()
                .filter(NotificationChannel::isEnabled)
                .forEach(channel -> {
                    NotificationLog.Status status = NotificationLog.Status.SENT;
                    String errorMsg = null;
                    try {
                        channel.send(payload);
                    } catch (Exception e) {
                        status = NotificationLog.Status.FAILED;
                        errorMsg = e.getMessage();
                        log.error("Channel {} failed to send notification for event {}: {}",
                                channel.getChannelName(), payload.getEventType(), e.getMessage());
                    }

                    // Persist log entry regardless of success/failure
                    try {
                        logRepository.save(NotificationLog.builder()
                                .societyId(payload.getSocietyId())
                                .flatId(payload.getFlatId())
                                .recipientEmail(payload.getRecipientEmail() != null
                                        ? payload.getRecipientEmail() : "unknown")
                                .subject(payload.getSubject())
                                .body(payload.getBody())
                                .channel(channel.getChannelName())
                                .eventType(payload.getEventType())
                                .status(status)
                                .errorMessage(errorMsg)
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to persist notification log: {}", e.getMessage());
                    }
                });
    }

    /**
     * Handle receipt.generated event payload directly (for Kafka consumer convenience).
     */
    public void handleReceiptGenerated(Map<String, Object> event) {
        try {
            Long societyId  = toLong(event.get("societyId"));
            Long flatId     = toLong(event.get("flatId"));
            String email    = getStr(event, "recipientEmail", getStr(event, "residentEmail", null));
            Object amount   = event.get("amount");
            Object month    = event.get("paymentMonth");
            Object year     = event.get("paymentYear");
            String receipt  = getStr(event, "receiptNumber", "");

            String body = String.format(
                    "Dear Resident,%n%nYour maintenance payment of \u20B9%s for %s/%s has been received.%n" +
                    "Receipt Number: %s%n%n" +
                    "Please download your receipt from the Society Ledger portal.%n%n" +
                    "Thank you,%nSociety Ledger",
                    amount, month, year, receipt);

            NotificationPayload payload = NotificationPayload.builder()
                    .recipientEmail(email)
                    .subject("Society Ledger \u2014 Maintenance Receipt Generated")
                    .body(body)
                    .eventType("RECEIPT_GENERATED")
                    .societyId(societyId)
                    .flatId(flatId)
                    .build();

            dispatch(payload);
        } catch (Exception e) {
            log.error("Error processing RECEIPT_GENERATED event: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast to all residents in a society.
     * TODO: Integrate with society-service Feign client to fetch resident emails.
     * Currently logs the intent; email list is empty until Feign integration is added.
     */
    public void notifyAllResidents(Long societyId, String subject, String body, String eventType) {
        log.info("Broadcasting {} to all residents of society {}: {}", eventType, societyId, subject);
        // Persist a single "broadcast" log entry indicating intent
        try {
            logRepository.save(NotificationLog.builder()
                    .societyId(societyId)
                    .recipientEmail("all-residents")
                    .subject(subject)
                    .body(body)
                    .channel("EMAIL")
                    .eventType(eventType)
                    .status(NotificationLog.Status.SKIPPED)
                    .errorMessage("Broadcast pending Feign integration with society-service")
                    .build());
        } catch (Exception e) {
            log.error("Failed to persist broadcast log: {}", e.getMessage());
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }
}
