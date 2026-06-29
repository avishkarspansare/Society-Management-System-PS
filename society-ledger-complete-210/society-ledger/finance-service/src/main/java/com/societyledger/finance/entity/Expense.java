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
        @Index(name = "idx_expense_society_status", columnList = "society_id,status"),
        @Index(name = "idx_expense_date", columnList = "expense_date")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Expense {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ExpenseCategory category;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "vendor_name", nullable = false, length = 255)
    private String vendorName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "proof_file_path", length = 512)
    private String proofFilePath;

    @Column(name = "proof_file_name", length = 255)
    private String proofFileName;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ExpenseStatus { DRAFT, PUBLISHED, ARCHIVED }
}
