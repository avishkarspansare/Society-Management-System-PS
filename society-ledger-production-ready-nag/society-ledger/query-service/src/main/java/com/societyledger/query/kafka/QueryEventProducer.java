package com.societyledger.query.kafka;

import com.societyledger.query.entity.PublicQuery;
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
public class QueryEventProducer {

    private static final String TOPIC = "query.answered";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishQueryAnswered(PublicQuery query) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId",      UUID.randomUUID().toString());
        event.put("eventType",    "QUERY_ANSWERED");
        event.put("societyId",    query.getSocietyId());
        event.put("queryId",      query.getId());
        event.put("flatId",       query.getFlatId());
        event.put("querySubject", query.getSubject());
        event.put("occurredAt",   Instant.now().toString());

        kafkaTemplate.send(TOPIC, String.valueOf(query.getSocietyId()), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish QUERY_ANSWERED: {}", ex.getMessage());
                    else log.info("Published QUERY_ANSWERED for query {}", query.getId());
                });
    }
}
