package com.societyledger.statement.kafka;

import com.societyledger.statement.dto.FlatPaymentRefDto;
import com.societyledger.statement.entity.PaymentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptEventProducer {

    private static final String TOPIC = "receipt.generated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentMatched(PaymentRecord payment, FlatPaymentRefDto flat) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "RECEIPT_GENERATED");
        event.put("societyId", payment.getSocietyId());
        event.put("flatId", payment.getFlatId());
        event.put("paymentId", payment.getId());
        event.put("amount", payment.getAmount());
        event.put("paymentMonth", payment.getPaymentMonth());
        event.put("paymentYear", payment.getPaymentYear());
        event.put("matchType", payment.getMatchType().name());
        event.put("residentEmail", flat.getEmail());
        event.put("occurredAt", Instant.now().toString());

        kafkaTemplate.send(TOPIC, String.valueOf(payment.getSocietyId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish RECEIPT_GENERATED event for payment {}: {}",
                                payment.getId(), ex.getMessage());
                    } else {
                        log.info("Published RECEIPT_GENERATED event for payment {} flat {}",
                                payment.getId(), payment.getFlatId());
                    }
                });
    }
}
