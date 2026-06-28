package com.societyledger.receipt.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReceiptResponse {
    private Long id;
    private Long societyId;
    private Long flatId;
    private Long paymentId;
    private String receiptNumber;
    private BigDecimal amount;
    private Integer paymentMonth;
    private Integer paymentYear;
    private String residentName;
    private String flatNumber;
    private String societyName;
    private Instant generatedAt;
    private boolean hasPdf;
}
