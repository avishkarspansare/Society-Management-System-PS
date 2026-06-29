package com.societyledger.finance.service;

import com.societyledger.finance.dto.response.FinancialDashboardResponse;
import com.societyledger.finance.dto.response.MonthlySummaryResponse;
import com.societyledger.finance.entity.Expense;
import com.societyledger.finance.entity.MonthlyFinancialSummary;
import com.societyledger.finance.feign.StatementServiceClient;
import com.societyledger.finance.feign.SocietyServiceClient;
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
    private final StatementServiceClient statementServiceClient;
    private final SocietyServiceClient societyServiceClient;

    /**
     * Regenerates/updates the monthly summary for a specific month.
     * Called after payment matching, expense publishing, etc.
     */
    @Transactional
    public void recomputeSummary(Long societyId, int year, int month) {
        // Fetch total income from Statement Service
        BigDecimal totalIncome = statementServiceClient.getTotalIncomeForMonth(societyId, year, month);

        // Compute total expenses from published expenses in the same month using streams
        BigDecimal totalExpenses = expenseRepository
                .findBySocietyIdAndStatus(societyId, Expense.ExpenseStatus.PUBLISHED)
                .stream()
                .filter(e -> e.getExpenseDate().getYear() == year
                        && e.getExpenseDate().getMonthValue() == month)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fetch pending (unpaid) flats count
        int totalFlats = societyServiceClient.getTotalFlatsCount(societyId);
        int paidFlats  = statementServiceClient.getPaidFlatsCountForMonth(societyId, year, month);
        int pendingFlats = Math.max(0, totalFlats - paidFlats);

        BigDecimal closingBalance = totalIncome.subtract(totalExpenses);

        MonthlyFinancialSummary summary = summaryRepository
                .findBySocietyIdAndYearAndMonth(societyId, year, month)
                .orElse(MonthlyFinancialSummary.builder()
                        .societyId(societyId)
                        .year(year)
                        .month(month)
                        .build());

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setClosingBalance(closingBalance);
        summary.setPendingFlats(pendingFlats);

        summaryRepository.save(summary);
        log.info("Monthly summary updated for society {} {}/{}: income={} expenses={} pending={}",
                societyId, month, year, totalIncome, totalExpenses, pendingFlats);
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getSummaries(Long societyId, Integer year) {
        List<MonthlyFinancialSummary> summaries = (year != null)
                ? summaryRepository.findBySocietyIdAndYearOrderByMonthDesc(societyId, year)
                : summaryRepository.findBySocietyIdOrderByYearDescMonthDesc(societyId);

        return summaries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FinancialDashboardResponse getDashboard(Long societyId) {
        LocalDate now = LocalDate.now();

        // Current month summary
        var currentSummary = summaryRepository
                .findBySocietyIdAndYearAndMonth(societyId, now.getYear(), now.getMonthValue());

        // YTD aggregation using streams
        List<MonthlyFinancialSummary> ytdSummaries = summaryRepository
                .findBySocietyIdAndYearOrderByMonthDesc(societyId, now.getYear());

        BigDecimal ytdIncome = ytdSummaries.stream()
                .map(MonthlyFinancialSummary::getTotalIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ytdExpenses = ytdSummaries.stream()
                .map(MonthlyFinancialSummary::getTotalExpenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recent published expenses for dashboard widget
        var recentExpenses = expenseRepository
                .findTop5BySocietyIdAndStatusOrderByPublishedAtDesc(
                        societyId, Expense.ExpenseStatus.PUBLISHED);

        return FinancialDashboardResponse.builder()
                .societyId(societyId)
                .currentMonth(now.getMonthValue())
                .currentYear(now.getYear())
                .currentMonthIncome(currentSummary.map(MonthlyFinancialSummary::getTotalIncome)
                        .orElse(BigDecimal.ZERO))
                .currentMonthExpenses(currentSummary.map(MonthlyFinancialSummary::getTotalExpenses)
                        .orElse(BigDecimal.ZERO))
                .currentMonthBalance(currentSummary.map(MonthlyFinancialSummary::getClosingBalance)
                        .orElse(BigDecimal.ZERO))
                .pendingFlats(currentSummary.map(MonthlyFinancialSummary::getPendingFlats)
                        .orElse(0))
                .ytdIncome(ytdIncome)
                .ytdExpenses(ytdExpenses)
                .ytdBalance(ytdIncome.subtract(ytdExpenses))
                .recentExpenses(recentExpenses.stream()
                        .map(e -> FinancialDashboardResponse.ExpenseSummary.builder()
                                .expenseId(e.getId())
                                .vendorName(e.getVendorName())
                                .amount(e.getAmount())
                                .categoryName(e.getCategory().getName())
                                .expenseDate(e.getExpenseDate())
                                .build())
                        .toList())
                .build();
    }

    private MonthlySummaryResponse mapToResponse(MonthlyFinancialSummary s) {
        return MonthlySummaryResponse.builder()
                .id(s.getId())
                .societyId(s.getSocietyId())
                .year(s.getYear())
                .month(s.getMonth())
                .totalIncome(s.getTotalIncome())
                .totalExpenses(s.getTotalExpenses())
                .closingBalance(s.getClosingBalance())
                .pendingFlats(s.getPendingFlats())
                .build();
    }
}
