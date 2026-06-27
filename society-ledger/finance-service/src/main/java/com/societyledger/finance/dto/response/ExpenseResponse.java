package com.societyledger.finance.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data @Builder
public class ExpenseResponse {
    Long id;
    Long societyId;
    Long categoryId;
    String categoryName;
    BigDecimal amount;
    String vendorName;
    String description;
    LocalDate expenseDate;
    String status;
    boolean hasProof;
    String proofFileName;
    Instant publishedAt;
    Instant createdAt;
}
