package com.societyledger.receipt.kafka;

import com.societyledger.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReceiptGenerated(Long societyId, Long receiptId, Long flatId, BigDecimal amount) {
        var event = Map.of(
            "eventType", "RECEIPT_GENERATED",
            "societyId", societyId,
            "receiptId", receiptId,
            "flatId",    flatId,
            "amount",    amount.toPlainString()
        );
        kafkaTemplate.send(KafkaTopics.RECEIPT_GENERATED, String.valueOf(societyId), event);
        log.info("Receipt-generated event sent: receipt={} society={}", receiptId, societyId);
    }
}
