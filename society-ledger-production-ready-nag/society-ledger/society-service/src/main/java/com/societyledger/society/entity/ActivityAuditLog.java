package com.societyledger.society.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "activity_audit_log",
    indexes = {
        @Index(name = "idx_aal_society", columnList = "society_id"),
        @Index(name = "idx_aal_user", columnList = "user_id"),
        @Index(name = "idx_aal_action", columnList = "action"),
        @Index(name = "idx_aal_occurred_at", columnList = "occurred_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "occurred_at")
    @Builder.Default
    private Instant occurredAt = Instant.now();
}
