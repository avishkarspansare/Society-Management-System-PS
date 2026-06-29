package com.societyledger.query.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "public_queries",
    indexes = {
        @Index(name = "idx_query_society", columnList = "society_id, status"),
        @Index(name = "idx_query_flat",    columnList = "flat_id"),
        @Index(name = "idx_query_created", columnList = "society_id, created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PublicQuery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_id", nullable = false)
    private Long flatId;

    @Column(name = "asked_by", nullable = false)
    private Long askedBy;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QueryStatus status = QueryStatus.OPEN;

    @OneToMany(mappedBy = "query", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QueryResponseEntity> responses = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum QueryStatus { OPEN, ANSWERED, CLOSED }
}
