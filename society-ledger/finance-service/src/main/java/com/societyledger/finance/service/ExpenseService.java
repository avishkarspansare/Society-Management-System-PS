package com.societyledger.finance.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.dto.request.CreateExpenseRequest;
import com.societyledger.finance.dto.response.ExpenseResponse;
import com.societyledger.finance.entity.Expense;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.kafka.ExpenseEventProducer;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import com.societyledger.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseEventProducer eventProducer;
    private final MonthlySummaryService summaryService;
    private final TimelineService timelineService;

    @Value("${app.file-storage.base-path}")
    private String basePath;

    @Transactional
    public ExpenseResponse createExpense(Long societyId, CreateExpenseRequest req, Long adminUserId) {
        ExpenseCategory category = categoryRepository.findByIdAndSocietyId(req.getCategoryId(), societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("ExpenseCategory", req.getCategoryId()));

        Expense saved = expenseRepository.save(Expense.builder()
                .societyId(societyId)
                .category(category)
                .amount(req.getAmount())
                .vendorName(req.getVendorName().strip())
                .description(req.getDescription().strip())
                .expenseDate(req.getExpenseDate())
                .status(Expense.ExpenseStatus.DRAFT)
                .createdBy(adminUserId)
                .build());

        timelineService.record(societyId, "EXPENSE_CREATED", adminUserId,
                "Expense ₹" + req.getAmount() + " to " + req.getVendorName() + " created",
                "Expense", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpenses(Long societyId, String status,
                                                      String role, Pageable pageable) {
        if ("RESIDENT".equals(role)) {
            // Residents only see PUBLISHED expenses
            return PageResponse.from(
                    expenseRepository.findBySocietyIdAndStatus(
                            societyId, Expense.ExpenseStatus.PUBLISHED, pageable)
                                     .map(this::mapToResponse));
        }
        if (status != null) {
            Expense.ExpenseStatus s = Expense.ExpenseStatus.valueOf(status.toUpperCase());
            return PageResponse.from(
                    expenseRepository.findBySocietyIdAndStatus(societyId, s, pageable)
                                     .map(this::mapToResponse));
        }
        return PageResponse.from(
                expenseRepository.findBySocietyIdOrderByExpenseDateDesc(societyId, pageable)
                                 .map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long societyId, Long expenseId, String role) {
        Expense expense = expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));
        if ("RESIDENT".equals(role) && expense.getStatus() != Expense.ExpenseStatus.PUBLISHED) {
            throw SocietyLedgerException.forbidden("Access denied.");
        }
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponse uploadProof(Long societyId, Long expenseId, MultipartFile file, Long adminUserId) {
        Expense expense = expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));

        if (file == null || file.isEmpty())
            throw SocietyLedgerException.badRequest("Proof file is empty.", "EMPTY_FILE");

        String stored = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path dir  = Paths.get(basePath, "expense-proofs", societyId.toString());
        Path dest = dir.resolve(stored);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SocietyLedgerException("Failed to store proof: " + e.getMessage(),
                    "FILE_STORE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        expense.setProofFilePath(dest.toString());
        expense.setProofFileName(file.getOriginalFilename());
        return mapToResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse publishExpense(Long societyId, Long expenseId, Long adminUserId) {
        Expense expense = expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));
        if (expense.getStatus() != Expense.ExpenseStatus.DRAFT)
            throw SocietyLedgerException.badRequest("Only DRAFT expenses can be published.", "INVALID_STATUS");

        expense.setStatus(Expense.ExpenseStatus.PUBLISHED);
        expense.setPublishedAt(Instant.now());
        Expense saved = expenseRepository.save(expense);

        // Update monthly summary
        summaryService.recordExpense(societyId,
                saved.getExpenseDate().getYear(),
                saved.getExpenseDate().getMonthValue(),
                saved.getAmount());

        // Publish Kafka event
        eventProducer.publishExpensePublished(societyId, saved.getId(),
                saved.getVendorName(), saved.getAmount());

        timelineService.record(societyId, "EXPENSE_PUBLISHED", adminUserId,
                "Expense ₹" + saved.getAmount() + " to " + saved.getVendorName() + " published",
                "Expense", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse archiveExpense(Long societyId, Long expenseId, Long adminUserId) {
        Expense expense = expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));
        expense.setStatus(Expense.ExpenseStatus.ARCHIVED);
        return mapToResponse(expenseRepository.save(expense));
    }

    private String sanitize(String name) {
        return name == null ? "proof" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private ExpenseResponse mapToResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId()).societyId(e.getSocietyId())
                .categoryId(e.getCategory() != null ? e.getCategory().getId() : null)
                .categoryName(e.getCategory() != null ? e.getCategory().getName() : "—")
                .amount(e.getAmount()).vendorName(e.getVendorName())
                .description(e.getDescription()).expenseDate(e.getExpenseDate())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .hasProof(e.getProofFilePath() != null)
                .proofFileName(e.getProofFileName())
                .publishedAt(e.getPublishedAt()).createdAt(e.getCreatedAt())
                .build();
    }
}
