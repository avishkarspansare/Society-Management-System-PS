package com.societyledger.finance.kafka;

import com.societyledger.finance.entity.Expense;
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
public class ExpenseEventProducer {

    private static final String EXPENSE_TOPIC = "expense.published";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpensePublished(Expense expense) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "EXPENSE_PUBLISHED");
        event.put("societyId", expense.getSocietyId());
        event.put("expenseId", expense.getId());
        event.put("amount", expense.getAmount());
        event.put("vendorName", expense.getVendorName());
        event.put("categoryName", expense.getCategory().getName());
        event.put("expenseDate", expense.getExpenseDate().toString());
        event.put("occurredAt", Instant.now().toString());

        kafkaTemplate.send(EXPENSE_TOPIC, String.valueOf(expense.getSocietyId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish EXPENSE_PUBLISHED event for expense {}: {}",
                                expense.getId(), ex.getMessage());
                    } else {
                        log.info("Published EXPENSE_PUBLISHED event for expense {} society {}",
                                expense.getId(), expense.getSocietyId());
                    }
                });
    }
}
