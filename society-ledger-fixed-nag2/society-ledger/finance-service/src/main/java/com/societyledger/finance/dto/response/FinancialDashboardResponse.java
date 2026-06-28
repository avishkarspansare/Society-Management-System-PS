package com.societyledger.finance.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDashboardResponse {

    private Long societyId;
    private Integer currentMonth;
    private Integer currentYear;
    private BigDecimal currentMonthIncome;
    private BigDecimal currentMonthExpenses;
    private BigDecimal currentMonthBalance;
    private Integer pendingFlats;
    private BigDecimal ytdIncome;
    private BigDecimal ytdExpenses;
    private BigDecimal ytdBalance;
    private List<ExpenseSummary> recentExpenses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSummary {
        private Long expenseId;
        private String vendorName;
        private BigDecimal amount;
        private String categoryName;
        private LocalDate expenseDate;
    }
}
