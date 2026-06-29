package com.societyledger.finance.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;

    @Transactional
    public ExpenseCategory create(Long societyId, String name, String description) {
        return repository.save(ExpenseCategory.builder()
                .societyId(societyId).name(name.strip()).description(description)
                .build());
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> getAll(Long societyId) {
        return repository.findBySocietyIdAndIsActiveTrueOrderByNameAsc(societyId);
    }
}
