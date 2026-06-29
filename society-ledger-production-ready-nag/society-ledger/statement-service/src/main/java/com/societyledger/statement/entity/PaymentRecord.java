package com.societyledger.statement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payment_records",
    indexes = {
        @Index(name = "idx_payment_society", columnList = "society_id"),
        @Index(name = "idx_payment_flat", columnList = "flat_id"),
        @Index(name = "idx_payment_month", columnList = "society_id, payment_year, payment_month")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_payment_flat_month",
        columnNames = {"flat_id", "payment_year", "payment_month", "payment_type"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_id", nullable = false)
    private Long flatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private BankTransaction transaction;

    @Column(name = "payment_month", nullable = false)
    private Integer paymentMonth;

    @Column(name = "payment_year", nullable = false)
    private Integer paymentYear;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_type", length = 30)
    @Builder.Default
    private String paymentType = "MAINTENANCE";

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum MatchType {
        AUTO, MANUAL
    }
}
