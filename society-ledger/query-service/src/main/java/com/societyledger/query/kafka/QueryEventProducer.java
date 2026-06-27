package com.societyledger.query.kafka;

import com.societyledger.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishQueryAnswered(Long societyId, Long queryId, Long askedByUserId) {
        var event = Map.of(
            "eventType",     "QUERY_ANSWERED",
            "societyId",     societyId,
            "queryId",       queryId,
            "askedByUserId", askedByUserId
        );
        kafkaTemplate.send(KafkaTopics.QUERY_EVENTS, String.valueOf(societyId), event);
        log.info("Query-answered event sent for query {} society {}", queryId, societyId);
    }
}
