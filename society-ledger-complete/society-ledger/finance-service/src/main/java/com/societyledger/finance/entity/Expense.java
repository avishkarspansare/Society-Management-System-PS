package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses",
    indexes = {
        @Index(name = "idx_expenses_society", columnList = "society_id"),
        @Index(name = "idx_expenses_status", columnList = "society_id, status"),
        @Index(name = "idx_expenses_date", columnList = "society_id, expense_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "vendor_name", nullable = false, length = 255)
    private String vendorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "proof_file_path", length = 500)
    private String proofFilePath;

    @Column(name = "proof_file_name", length = 255)
    private String proofFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ExpenseStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public boolean hasProof() {
        return proofFilePath != null && !proofFilePath.isBlank();
    }
}
