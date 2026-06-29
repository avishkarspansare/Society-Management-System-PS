package com.societyledger.receipt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "receipts",
    indexes = {
        @Index(name = "idx_receipt_society", columnList = "society_id"),
        @Index(name = "idx_receipt_flat", columnList = "flat_id"),
        @Index(name = "idx_receipt_payment", columnList = "payment_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_id", nullable = false)
    private Long flatId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_month", nullable = false)
    private Integer paymentMonth;

    @Column(name = "payment_year", nullable = false)
    private Integer paymentYear;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    @Column(name = "resident_name", length = 255)
    private String residentName;

    @Column(name = "flat_number", length = 50)
    private String flatNumber;

    @Column(name = "society_name", length = 255)
    private String societyName;

    @Column(name = "generated_at")
    @Builder.Default
    private Instant generatedAt = Instant.now();
}
