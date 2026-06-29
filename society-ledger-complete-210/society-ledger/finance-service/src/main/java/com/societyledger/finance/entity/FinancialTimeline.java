package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "financial_timeline",
    indexes = {
        @Index(name = "idx_timeline_society", columnList = "society_id"),
        @Index(name = "idx_timeline_created", columnList = "created_at")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FinancialTimeline {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @Column(name = "actor_user_id") private Long actorUserId;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "ref_entity_type", length = 100) private String refEntityType;
    @Column(name = "ref_entity_id") private Long refEntityId;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
