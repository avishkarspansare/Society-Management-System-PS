package com.societyledger.statement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bank_transactions",
    indexes = {
        @Index(name = "idx_txn_statement", columnList = "statement_id"),
        @Index(name = "idx_txn_society_status", columnList = "society_id,match_status"),
        @Index(name = "idx_txn_date", columnList = "transaction_date")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankTransaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "statement_id", nullable = false) private Long statementId;
    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "transaction_date") private LocalDate transactionDate;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "amount", precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "reference_code", length = 100) private String referenceCode;
    @Column(name = "matched_flat_id") private Long matchedFlatId;
    @Column(name = "matched_by") private Long matchedBy;
    @Column(name = "matched_at") private Instant matchedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", length = 50)
    @Builder.Default
    private MatchStatus matchStatus = MatchStatus.UNMATCHED;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum MatchStatus { UNMATCHED, AUTO_MATCHED, MANUALLY_MATCHED, MATCHED, IGNORED }
}
