package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "monthly_financial_summary",
    uniqueConstraints = @UniqueConstraint(name = "uq_summary_month", columnNames = {"society_id","year","month"})
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MonthlyFinancialSummary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(name = "society_id", nullable = false)
    private Long societyId;
    @Column(nullable = false)
    private Integer year;
    @Column(nullable = false)
    private Integer month;
    @Column(name = "total_income", precision = 15, scale = 2) @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;
    @Column(name = "total_expenses", precision = 15, scale = 2) @Builder.Default
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    @Column(name = "closing_balance", precision = 15, scale = 2) @Builder.Default
    private BigDecimal closingBalance = BigDecimal.ZERO;
    @Column(name = "pending_flats") @Builder.Default
    private Integer pendingFlats = 0;
    @Column(name = "generated_at") @Builder.Default
    private Instant generatedAt = Instant.now();
    @Column(name = "updated_at") @Builder.Default
    private Instant updatedAt = Instant.now();
}
