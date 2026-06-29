package com.societyledger.finance.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private Long societyId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String vendorName;
    private String description;
    private LocalDate expenseDate;
    private String status;
    private boolean hasProof;
    private String proofFileName;
    private Instant publishedAt;
    private Instant createdAt;
}
