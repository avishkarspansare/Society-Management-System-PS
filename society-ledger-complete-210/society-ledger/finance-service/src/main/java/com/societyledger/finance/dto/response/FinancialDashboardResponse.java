package com.societyledger.finance.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class FinancialDashboardResponse {
    Long societyId;
    int currentMonth;
    int currentYear;
    BigDecimal currentMonthIncome;
    BigDecimal currentMonthExpenses;
    BigDecimal currentMonthBalance;
    int pendingFlats;
    BigDecimal ytdIncome;
    BigDecimal ytdExpenses;
    BigDecimal ytdBalance;
    List<ExpenseSummary> recentExpenses;

    @Data @Builder
    public static class ExpenseSummary {
        Long expenseId;
        String vendorName;
        BigDecimal amount;
        String categoryName;
        LocalDate expenseDate;
    }
}
