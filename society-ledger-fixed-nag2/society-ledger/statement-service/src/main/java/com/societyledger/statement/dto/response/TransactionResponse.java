package com.societyledger.statement.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long societyId;
    private LocalDate transactionDate;
    private String description;
    private String referenceNumber;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private BigDecimal balance;
    private String matchStatus;
}
