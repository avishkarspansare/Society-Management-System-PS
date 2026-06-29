package com.societyledger.finance.service;

import com.societyledger.finance.dto.response.FinancialDashboardResponse;
import com.societyledger.finance.dto.response.MonthlySummaryResponse;
import com.societyledger.finance.entity.Expense;
import com.societyledger.finance.entity.MonthlyFinancialSummary;
import com.societyledger.finance.repository.ExpenseRepository;
import com.societyledger.finance.repository.MonthlySummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlySummaryService {

    private final MonthlySummaryRepository summaryRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public FinancialDashboardResponse getDashboard(Long societyId) {
        LocalDate now = LocalDate.now();
        int year  = now.getYear();
        int month = now.getMonthValue();

        MonthlyFinancialSummary current = summaryRepository
                .findBySocietyIdAndYearAndMonth(societyId, year, month)
                .orElseGet(() -> MonthlyFinancialSummary.builder()
                        .societyId(societyId).year(year).month(month).build());

        // YTD: sum all summaries for the current year
        List<MonthlyFinancialSummary> ytd =
                summaryRepository.findBySocietyIdAndYearOrderByMonthDesc(societyId, year);
        BigDecimal ytdIncome   = ytd.stream().map(MonthlyFinancialSummary::getTotalIncome)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ytdExpenses = ytd.stream().map(MonthlyFinancialSummary::getTotalExpenses)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recent published expenses
        List<Expense> recent = expenseRepository
                .findTop5BySocietyIdAndStatusOrderByPublishedAtDesc(societyId, Expense.ExpenseStatus.PUBLISHED);

        List<FinancialDashboardResponse.ExpenseSummary> expenseSummaries = recent.stream()
                .map(e -> FinancialDashboardResponse.ExpenseSummary.builder()
                        .expenseId(e.getId()).vendorName(e.getVendorName())
                        .amount(e.getAmount())
                        .categoryName(e.getCategory() != null ? e.getCategory().getName() : "—")
                        .expenseDate(e.getExpenseDate())
                        .build())
                .collect(Collectors.toList());

        return FinancialDashboardResponse.builder()
                .societyId(societyId).currentMonth(month).currentYear(year)
                .currentMonthIncome(current.getTotalIncome())
                .currentMonthExpenses(current.getTotalExpenses())
                .currentMonthBalance(current.getClosingBalance())
                .pendingFlats(current.getPendingFlats())
                .ytdIncome(ytdIncome).ytdExpenses(ytdExpenses)
                .ytdBalance(ytdIncome.subtract(ytdExpenses))
                .recentExpenses(expenseSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getSummaries(Long societyId, Integer year) {
        List<MonthlyFinancialSummary> summaries = (year != null)
                ? summaryRepository.findBySocietyIdAndYearOrderByMonthDesc(societyId, year)
                : summaryRepository.findBySocietyIdOrderByYearDescMonthDesc(societyId);
        return summaries.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Called by StatementService after payment matching to update the summary.
     * Uses upsert pattern: create if not present, add income if present.
     */
    @Transactional
    public void recordIncome(Long societyId, int year, int month, BigDecimal amount) {
        MonthlyFinancialSummary summary = summaryRepository
                .findBySocietyIdAndYearAndMonth(societyId, year, month)
                .orElseGet(() -> MonthlyFinancialSummary.builder()
                        .societyId(societyId).year(year).month(month).build());
        summary.setTotalIncome(summary.getTotalIncome().add(amount));
        summary.setClosingBalance(summary.getTotalIncome().subtract(summary.getTotalExpenses()));
        summaryRepository.save(summary);
    }

    @Transactional
    public void recordExpense(Long societyId, int year, int month, BigDecimal amount) {
        MonthlyFinancialSummary summary = summaryRepository
                .findBySocietyIdAndYearAndMonth(societyId, year, month)
                .orElseGet(() -> MonthlyFinancialSummary.builder()
                        .societyId(societyId).year(year).month(month).build());
        summary.setTotalExpenses(summary.getTotalExpenses().add(amount));
        summary.setClosingBalance(summary.getTotalIncome().subtract(summary.getTotalExpenses()));
        summaryRepository.save(summary);
    }

    private MonthlySummaryResponse mapToResponse(MonthlyFinancialSummary s) {
        return MonthlySummaryResponse.builder()
                .id(s.getId()).societyId(s.getSocietyId())
                .year(s.getYear()).month(s.getMonth())
                .totalIncome(s.getTotalIncome()).totalExpenses(s.getTotalExpenses())
                .closingBalance(s.getClosingBalance()).pendingFlats(s.getPendingFlats())
                .build();
    }
}
