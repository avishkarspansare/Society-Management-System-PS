package com.societyledger.statement.dto.response;

import com.societyledger.statement.entity.BankTransaction;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class TransactionResponse {
    Long id;
    Long statementId;
    Long societyId;
    LocalDate transactionDate;
    String description;
    BigDecimal amount;
    String referenceCode;
    BankTransaction.MatchStatus matchStatus;
    Long matchedFlatId;
}
