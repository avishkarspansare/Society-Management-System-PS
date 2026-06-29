package com.societyledger.finance.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TimelineResponse {
    private Long id;
    private Long societyId;
    private String eventType;
    private String eventSummary;
    private Long referenceId;
    private String referenceType;
    private Instant occurredAt;
}
