package com.societyledger.finance.repository;
import com.societyledger.finance.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findBySocietyIdAndStatus(Long societyId, Expense.ExpenseStatus status, Pageable p);
    Page<Expense> findBySocietyIdOrderByExpenseDateDesc(Long societyId, Pageable p);
    List<Expense> findBySocietyIdAndStatus(Long societyId, Expense.ExpenseStatus status);
    List<Expense> findTop5BySocietyIdAndStatusOrderByPublishedAtDesc(Long societyId, Expense.ExpenseStatus status);
    Optional<Expense> findByIdAndSocietyId(Long id, Long societyId);
}
