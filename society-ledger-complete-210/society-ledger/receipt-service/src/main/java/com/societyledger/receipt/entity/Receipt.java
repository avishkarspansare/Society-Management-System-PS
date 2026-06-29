package com.societyledger.receipt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts",
    uniqueConstraints = @UniqueConstraint(name = "uq_receipt_txn", columnNames = "transaction_id"),
    indexes = {
        @Index(name = "idx_receipt_society_flat", columnList = "society_id,flat_id"),
        @Index(name = "idx_receipt_month_year", columnList = "month,year")
    })
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Receipt {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false) private Long societyId;
    @Column(name = "flat_id", nullable = false)    private Long flatId;
    @Column(name = "transaction_id")               private Long transactionId;

    @Column(name = "receipt_number", unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", length = 20)
    private String transactionDate;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "flat_number", length = 50)  private String flatNumber;
    @Column(name = "wing_name", length = 100)   private String wingName;
    @Column(name = "month")                      private Integer month;
    @Column(name = "year")                       private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private ReceiptStatus status = ReceiptStatus.GENERATED;

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum ReceiptStatus { GENERATED, SENT, CANCELLED }
}
