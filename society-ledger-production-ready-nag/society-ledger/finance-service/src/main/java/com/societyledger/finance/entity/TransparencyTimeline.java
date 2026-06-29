package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "transparency_timeline",
    indexes = @Index(name = "idx_timeline_society", columnList = "society_id")
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransparencyTimeline {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "society_id", nullable = false)
    private Long societyId;
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    @Column(name = "event_summary", nullable = false, length = 500)
    private String eventSummary;
    @Column(name = "reference_id")
    private Long referenceId;
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    @Column(name = "actor_user_id")
    private Long actorUserId;
    @Column(name = "occurred_at") @Builder.Default
    private Instant occurredAt = Instant.now();
}
