package com.societyledger.finance.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class MonthlySummaryResponse {
    Long id;
    Long societyId;
    int year;
    int month;
    BigDecimal totalIncome;
    BigDecimal totalExpenses;
    BigDecimal closingBalance;
    int pendingFlats;
}
