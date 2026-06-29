package com.societyledger.query.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Entity
@Table(name = "resident_queries",
    indexes = {
        @Index(name = "idx_rq_society", columnList = "society_id"),
        @Index(name = "idx_rq_status", columnList = "status")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResidentQuery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "flat_id") private Long flatId;
    @Column(name = "asked_by_user_id", nullable = false) private Long askedByUserId;
    @Column(name = "subject", nullable = false, length = 255) private String subject;
    @Column(name = "body", columnDefinition = "TEXT", nullable = false) private String body;
    @Column(name = "answer", columnDefinition = "TEXT") private String answer;
    @Column(name = "answered_by") private Long answeredBy;
    @Column(name = "answered_at") private Instant answeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private QueryStatus status = QueryStatus.OPEN;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum QueryStatus { OPEN, IN_PROGRESS, ANSWERED, CLOSED }
}
