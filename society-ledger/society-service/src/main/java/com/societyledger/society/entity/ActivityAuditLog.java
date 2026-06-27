package com.societyledger.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "activity_audit_logs",
    indexes = {
        @Index(name = "idx_aal_society", columnList = "society_id"),
        @Index(name = "idx_aal_created_at", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityAuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
