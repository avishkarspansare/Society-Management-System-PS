package com.societyledger.finance.dto.response;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonthlySummaryResponse {
    private Long id;
    private Long societyId;
    private Integer year;
    private Integer month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal closingBalance;
    private Integer pendingFlats;
}
