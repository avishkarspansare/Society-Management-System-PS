package com.societyledger.statement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "bank_statements",
    indexes = @Index(name = "idx_stmt_society", columnList = "society_id"))
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankStatement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "bank_code", nullable = false, length = 20) private String bankCode;
    @Column(name = "file_name", length = 255) private String fileName;
    @Column(name = "file_path", length = 512) private String filePath;
    @Column(name = "uploaded_by") private Long uploadedBy;
    @Column(name = "total_transactions") private Integer totalTransactions;
    @Column(name = "matched_count") @Builder.Default private Integer matchedCount = 0;
    @Column(name = "unmatched_count") @Builder.Default private Integer unmatchedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private StatementStatus status = StatementStatus.UPLOADED;

    @CreatedDate @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;

    public enum StatementStatus { UPLOADED, PARSED, MATCHED, COMPLETED }
}
