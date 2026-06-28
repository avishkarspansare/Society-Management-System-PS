package com.societyledger.audit.kafka;

import com.societyledger.audit.entity.AuditReport;
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
public class AuditEventProducer {

    private static final String TOPIC = "audit.uploaded";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishAuditUploaded(AuditReport report) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "AUDIT_UPLOADED");
        event.put("societyId", report.getSocietyId());
        event.put("auditReportId", report.getId());
        event.put("auditYear", report.getAuditYear());
        event.put("auditorName", report.getAuditorName());
        event.put("complianceStatus", report.getComplianceStatus().name());
        event.put("occurredAt", Instant.now().toString());

        kafkaTemplate.send(TOPIC, String.valueOf(report.getSocietyId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish AUDIT_UPLOADED event: {}", ex.getMessage());
                    } else {
                        log.info("Published AUDIT_UPLOADED event for report {} society {}",
                                report.getId(), report.getSocietyId());
                    }
                });
    }
}
