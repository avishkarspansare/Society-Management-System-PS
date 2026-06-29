package com.societyledger.query.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "query_responses",
    indexes = @Index(name = "idx_qresp_query", columnList = "query_id")
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QueryResponseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id", nullable = false)
    private PublicQuery query;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "responded_by", nullable = false)
    private Long respondedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(name = "created_at") @Builder.Default
    private Instant createdAt = Instant.now();
}
