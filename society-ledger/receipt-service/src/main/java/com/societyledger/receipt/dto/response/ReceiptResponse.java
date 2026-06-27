package com.societyledger.receipt.dto.response;

import com.societyledger.receipt.entity.Receipt;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class ReceiptResponse {
    Long id;
    Long societyId;
    Long flatId;
    Long transactionId;
    String receiptNumber;
    BigDecimal amount;
    String transactionDate;
    String referenceCode;
    String flatNumber;
    String wingName;
    Integer month;
    Integer year;
    Receipt.ReceiptStatus status;
    Instant createdAt;
}
