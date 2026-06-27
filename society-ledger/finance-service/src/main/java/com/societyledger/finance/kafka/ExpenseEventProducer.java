package com.societyledger.finance.kafka;

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
public class ExpenseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpensePublished(Long societyId, Long expenseId,
                                         String vendorName, BigDecimal amount) {
        var event = Map.of(
            "eventType",  "EXPENSE_PUBLISHED",
            "societyId",  societyId,
            "expenseId",  expenseId,
            "vendorName", vendorName,
            "amount",     amount.toPlainString()
        );
        kafkaTemplate.send(KafkaTopics.EXPENSE_PUBLISHED, String.valueOf(societyId), event);
        log.info("Expense-published event sent: expense={} society={}", expenseId, societyId);
    }
}
