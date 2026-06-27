package com.societyledger.notification.kafka;

import com.societyledger.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "society-ledger.receipt.generated", groupId = "notification-service")
    public void onReceiptGenerated(@Payload Map<String, Object> event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Long societyId = toLong(event.get("societyId"));
            Long flatId    = toLong(event.get("flatId"));
            Long receiptId = toLong(event.get("receiptId"));
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            emailService.sendReceiptNotification(societyId, flatId, receiptId, amount);
        } catch (Exception e) {
            log.error("Failed to send receipt notification for event {}: {}", event, e.getMessage());
        }
    }

    @KafkaListener(topics = "society-ledger.query.events", groupId = "notification-service")
    public void onQueryAnswered(@Payload Map<String, Object> event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            if (!"QUERY_ANSWERED".equals(event.get("eventType"))) return;
            Long societyId     = toLong(event.get("societyId"));
            Long queryId       = toLong(event.get("queryId"));
            Long askedByUserId = toLong(event.get("askedByUserId"));
            emailService.sendQueryAnsweredNotification(societyId, queryId, askedByUserId);
        } catch (Exception e) {
            log.error("Failed to send query-answered notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "society-ledger.audit.events", groupId = "notification-service")
    public void onAuditUploaded(@Payload Map<String, Object> event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            if (!"AUDIT_UPLOADED".equals(event.get("eventType"))) return;
            Long societyId = toLong(event.get("societyId"));
            String title   = event.get("title").toString();
            emailService.sendAuditUploadedNotification(societyId, title);
        } catch (Exception e) {
            log.error("Failed to send audit-uploaded notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "society-ledger.expense.published", groupId = "notification-service")
    public void onExpensePublished(@Payload Map<String, Object> event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Long societyId  = toLong(event.get("societyId"));
            Long expenseId  = toLong(event.get("expenseId"));
            String vendor   = event.get("vendorName").toString();
            BigDecimal amt  = new BigDecimal(event.get("amount").toString());
            emailService.sendExpensePublishedNotification(societyId, expenseId, vendor, amt);
        } catch (Exception e) {
            log.error("Failed to send expense-published notification: {}", e.getMessage());
        }
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
