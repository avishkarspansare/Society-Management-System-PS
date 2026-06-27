package com.societyledger.finance.repository;

import com.societyledger.finance.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    Optional<ExpenseCategory> findByIdAndSocietyId(Long id, Long societyId);
    List<ExpenseCategory> findBySocietyIdAndIsActiveTrueOrderByNameAsc(Long societyId);
}
