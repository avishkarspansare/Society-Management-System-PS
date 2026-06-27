package com.societyledger.receipt.kafka;

import com.societyledger.common.kafka.KafkaTopics;
import com.societyledger.receipt.service.ReceiptService;
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
public class ReceiptEventConsumer {

    private final ReceiptService receiptService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_MATCHED, groupId = "receipt-service")
    public void onPaymentMatched(@Payload Map<String, Object> event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Long societyId   = toLong(event.get("societyId"));
            Long flatId      = toLong(event.get("flatId"));
            Long txnId       = toLong(event.get("transactionId"));
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String txnDate   = event.get("transactionDate").toString();
            String refCode   = event.get("referenceCode") != null
                               ? event.get("referenceCode").toString() : null;

            receiptService.generateReceipt(societyId, flatId, txnId, amount, txnDate, refCode);
            log.info("Receipt generated for society {} flat {} txn {}", societyId, flatId, txnId);
        } catch (Exception e) {
            log.error("Failed to process PAYMENT_MATCHED event: {}", event, e);
        }
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
