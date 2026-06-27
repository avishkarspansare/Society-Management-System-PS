package com.societyledger.audit.kafka;

import com.societyledger.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAuditUploaded(Long societyId, Long auditReportId, String title) {
        var event = Map.of(
            "eventType",     "AUDIT_UPLOADED",
            "societyId",     societyId,
            "auditReportId", auditReportId,
            "title",         title
        );
        kafkaTemplate.send(KafkaTopics.AUDIT_EVENTS, String.valueOf(societyId), event);
        log.info("Audit-uploaded event sent for society {} report {}", societyId, auditReportId);
    }
}
