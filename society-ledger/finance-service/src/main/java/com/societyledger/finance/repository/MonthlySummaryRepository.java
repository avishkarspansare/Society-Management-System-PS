package com.societyledger.finance.repository;

import com.societyledger.finance.entity.MonthlyFinancialSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlyFinancialSummary, Long> {
    Optional<MonthlyFinancialSummary> findBySocietyIdAndYearAndMonth(Long sid, int year, int month);
    List<MonthlyFinancialSummary> findBySocietyIdAndYearOrderByMonthDesc(Long sid, int year);
    List<MonthlyFinancialSummary> findBySocietyIdOrderByYearDescMonthDesc(Long sid);
}
