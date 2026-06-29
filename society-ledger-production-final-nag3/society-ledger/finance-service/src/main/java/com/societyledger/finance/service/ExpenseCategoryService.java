package com.societyledger.finance.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.dto.request.CreateCategoryRequest;
import com.societyledger.finance.dto.response.ExpenseCategoryResponse;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> getCategories(Long societyId) {
        return categoryRepository.findBySocietyId(societyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseCategoryResponse create(Long societyId, CreateCategoryRequest request) {
        if (categoryRepository.existsBySocietyIdAndNameIgnoreCase(societyId, request.getName())) {
            throw new SocietyLedgerException(
                    "Category '" + request.getName() + "' already exists.",
                    "DUPLICATE_CATEGORY",
                    HttpStatus.CONFLICT
            );
        }
        ExpenseCategory saved = categoryRepository.save(
                ExpenseCategory.builder()
                        .societyId(societyId)
                        .name(request.getName().trim())
                        .description(request.getDescription())
                        .build()
        );
        log.info("Expense category created: {} for society {}", saved.getName(), societyId);
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long societyId, Long categoryId) {
        ExpenseCategory category = categoryRepository.findByIdAndSocietyId(categoryId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("ExpenseCategory", categoryId));
        categoryRepository.delete(category);
        log.info("Expense category {} deleted for society {}", categoryId, societyId);
    }

    private ExpenseCategoryResponse mapToResponse(ExpenseCategory c) {
        return ExpenseCategoryResponse.builder()
                .id(c.getId())
                .societyId(c.getSocietyId())
                .name(c.getName())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
