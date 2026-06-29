package com.societyledger.statement.dto.response;

import com.societyledger.statement.entity.BankStatement;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class BankStatementResponse {
    Long id;
    Long societyId;
    String bankCode;
    String fileName;
    Long uploadedBy;
    Integer totalTransactions;
    Integer matchedCount;
    Integer unmatchedCount;
    BankStatement.StatementStatus status;
    Instant uploadedAt;
}
