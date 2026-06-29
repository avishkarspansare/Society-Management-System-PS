package com.societyledger.statement.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity
@Table(name = "unmatched_transactions",
    indexes = @Index(name = "idx_unmatched_society", columnList = "society_id, resolved")
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UnmatchedTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BankTransaction transaction;
    @Column(name = "society_id", nullable = false)
    private Long societyId;
    @Column(length = 255)
    private String reason;
    @Builder.Default
    private Boolean resolved = false;
    @Column(name = "resolved_by")
    private Long resolvedBy;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    @Column(name = "created_at") @Builder.Default
    private Instant createdAt = Instant.now();
}
