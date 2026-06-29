package com.societyledger.notification.kafka;

import com.societyledger.notification.service.NotificationDispatchService;
import com.societyledger.notification.strategy.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for all Society Ledger notification events.
 *
 * Topics consumed:
 * - receipt.generated     → Send receipt confirmation to resident
 * - expense.published     → Notify all residents of new expense
 * - audit.uploaded        → Notify all residents of new audit report
 * - query.answered        → Notify the resident who raised the query
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationDispatchService dispatchService;

    @KafkaListener(topics = "receipt.generated", groupId = "notification-service")
    public void onReceiptGenerated(@Payload Map<String, Object> event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received event from topic {}: {}", topic, event.get("eventType"));
        try {
            Long societyId  = toLong(event.get("societyId"));
            Long flatId     = toLong(event.get("flatId"));
            String email    = (String) event.get("residentEmail");
            Object amount   = event.get("amount");
            Object month    = event.get("paymentMonth");
            Object year     = event.get("paymentYear");

            String body = String.format(
                    "Dear Resident,\n\nYour maintenance payment of ₹%s for %s/%s has been received.\n" +
                    "Please download your receipt from the Society Ledger portal.\n\n" +
                    "Thank you,\nSociety Ledger",
                    amount, month, year);

            NotificationPayload payload = NotificationPayload.builder()
                    .recipientEmail(email)
                    .subject("Society Ledger — Maintenance Receipt Generated")
                    .body(body)
                    .eventType("RECEIPT_GENERATED")
                    .societyId(societyId)
                    .flatId(flatId)
                    .build();

            dispatchService.dispatch(payload);
        } catch (Exception e) {
            log.error("Error processing RECEIPT_GENERATED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "expense.published", groupId = "notification-service")
    public void onExpensePublished(@Payload Map<String, Object> event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received event from topic {}: {}", topic, event.get("eventType"));
        try {
            Long societyId = toLong(event.get("societyId"));
            Object amount  = event.get("amount");
            String vendor  = (String) event.get("vendorName");
            String cat     = (String) event.get("categoryName");

            // Notify all residents in the society — dispatch service fetches emails
            dispatchService.notifyAllResidents(societyId,
                    "Society Ledger — New Expense Published",
                    String.format("A new expense of ₹%s has been published.\n" +
                            "Vendor: %s | Category: %s\n\n" +
                            "Log in to Society Ledger for full details.", amount, vendor, cat),
                    "EXPENSE_PUBLISHED");
        } catch (Exception e) {
            log.error("Error processing EXPENSE_PUBLISHED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "audit.uploaded", groupId = "notification-service")
    public void onAuditUploaded(@Payload Map<String, Object> event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received event from topic {}: {}", topic, event.get("eventType"));
        try {
            Long societyId  = toLong(event.get("societyId"));
            Object auditYear = event.get("auditYear");
            String auditor  = (String) event.get("auditorName");
            String status   = (String) event.get("complianceStatus");

            dispatchService.notifyAllResidents(societyId,
                    "Society Ledger — Audit Report Uploaded",
                    String.format("The audit report for %s is now available.\n" +
                            "Auditor: %s | Status: %s\n\n" +
                            "Log in to Society Ledger to view the report.", auditYear, auditor, status),
                    "AUDIT_UPLOADED");
        } catch (Exception e) {
            log.error("Error processing AUDIT_UPLOADED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "query.answered", groupId = "notification-service")
    public void onQueryAnswered(@Payload Map<String, Object> event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received event from topic {}: {}", topic, event.get("eventType"));
        try {
            Long societyId = toLong(event.get("societyId"));
            Long flatId    = toLong(event.get("flatId"));
            String email   = (String) event.get("askerEmail");
            String subject = (String) event.get("querySubject");

            NotificationPayload payload = NotificationPayload.builder()
                    .recipientEmail(email)
                    .subject("Society Ledger — Your Query Has Been Answered")
                    .body(String.format("Dear Resident,\n\nYour query '%s' has been answered " +
                            "by the admin.\n\nLog in to Society Ledger to view the response.\n\n" +
                            "Thank you.", subject))
                    .eventType("QUERY_ANSWERED")
                    .societyId(societyId)
                    .flatId(flatId)
                    .build();

            dispatchService.dispatch(payload);
        } catch (Exception e) {
            log.error("Error processing QUERY_ANSWERED event: {}", e.getMessage(), e);
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
