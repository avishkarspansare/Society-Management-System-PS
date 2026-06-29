package com.societyledger.statement.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BankStatementResponse {
    private Long id;
    private Long societyId;
    private String bankName;
    private Integer statementMonth;
    private Integer statementYear;
    private String fileName;
    private String uploadStatus;
    private Long uploadedBy;
    private Instant uploadedAt;
    private Instant processedAt;
    private int transactionCount;
}
