package com.societyledger.finance.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.dto.request.CreateExpenseRequest;
import com.societyledger.finance.dto.request.UpdateExpenseRequest;
import com.societyledger.finance.dto.response.ExpenseResponse;
import com.societyledger.finance.entity.Expense;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.kafka.ExpenseEventProducer;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import com.societyledger.finance.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final String PROOF_DIR = System.getProperty("user.home") + "/society-ledger/expense-proofs/";
    private static final List<String> ALLOWED_PROOF_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_PROOF_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseEventProducer eventProducer;
    private final TimelineService timelineService;

    @Transactional
    public ExpenseResponse createExpense(Long societyId, CreateExpenseRequest request,
                                          Long adminUserId) {
        ExpenseCategory category = categoryRepository
                .findByIdAndSocietyId(request.getCategoryId(), societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("ExpenseCategory",
                        request.getCategoryId()));

        Expense expense = Expense.builder()
                .societyId(societyId)
                .category(category)
                .amount(request.getAmount())
                .vendorName(request.getVendorName().strip())
                .description(request.getDescription().strip())
                .expenseDate(request.getExpenseDate())
                .createdBy(adminUserId)
                .status(Expense.ExpenseStatus.DRAFT)
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense DRAFT created: {} ₹{} for society {}", saved.getId(), saved.getAmount(), societyId);

        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse uploadProof(Long societyId, Long expenseId,
                                        MultipartFile proofFile, Long adminUserId) {
        Expense expense = getExpenseForAdmin(societyId, expenseId);

        if (expense.getStatus() == Expense.ExpenseStatus.ARCHIVED) {
            throw SocietyLedgerException.badRequest("Cannot update an archived expense.");
        }

        // Validate proof file
        validateProofFile(proofFile);

        // Save proof file
        String filePath = saveProofFile(societyId, expenseId, proofFile);

        expense.setProofFilePath(filePath);
        expense.setProofFileName(proofFile.getOriginalFilename());

        return mapToResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse publishExpense(Long societyId, Long expenseId, Long adminUserId) {
        Expense expense = getExpenseForAdmin(societyId, expenseId);

        if (expense.getStatus() != Expense.ExpenseStatus.DRAFT) {
            throw SocietyLedgerException.badRequest(
                    "Only DRAFT expenses can be published. Current status: " + expense.getStatus());
        }

        // Proof is MANDATORY before publishing
        if (!expense.hasProof()) {
            throw new SocietyLedgerException(
                    "Expense proof is mandatory before publishing. Please upload a proof document.",
                    "PROOF_REQUIRED",
                    HttpStatus.BAD_REQUEST
            );
        }

        expense.setStatus(Expense.ExpenseStatus.PUBLISHED);
        expense.setPublishedAt(Instant.now());
        Expense saved = expenseRepository.save(expense);

        // Add to transparency timeline
        timelineService.record(societyId, "EXPENSE_PUBLISHED", adminUserId,
                String.format("Expense of ₹%.2f published: %s (%s)",
                        saved.getAmount(), saved.getVendorName(), saved.getCategory().getName()),
                "EXPENSE", saved.getId());

        // Publish Kafka event → notify residents
        eventProducer.publishExpensePublished(saved);

        log.info("Expense {} published for society {}", expenseId, societyId);
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse archiveExpense(Long societyId, Long expenseId, Long adminUserId) {
        Expense expense = getExpenseForAdmin(societyId, expenseId);
        expense.setStatus(Expense.ExpenseStatus.ARCHIVED);
        return mapToResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpenses(Long societyId, String status,
                                                      String role, Pageable pageable) {
        // Residents only see PUBLISHED expenses
        if ("RESIDENT".equals(role)) {
            return PageResponse.from(
                    expenseRepository.findBySocietyIdAndStatus(
                            societyId, Expense.ExpenseStatus.PUBLISHED, pageable)
                            .map(this::mapToResponse)
            );
        }

        // Admin sees by status filter or all
        if (status != null && !status.isBlank()) {
            Expense.ExpenseStatus statusEnum = Expense.ExpenseStatus.valueOf(status.toUpperCase());
            return PageResponse.from(
                    expenseRepository.findBySocietyIdAndStatus(societyId, statusEnum, pageable)
                            .map(this::mapToResponse)
            );
        }

        return PageResponse.from(
                expenseRepository.findBySocietyIdOrderByExpenseDateDesc(societyId, pageable)
                        .map(this::mapToResponse)
        );
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long societyId, Long expenseId, String role) {
        Expense expense = expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));

        if ("RESIDENT".equals(role) && expense.getStatus() != Expense.ExpenseStatus.PUBLISHED) {
            throw SocietyLedgerException.forbidden("This expense is not available for viewing.");
        }

        return mapToResponse(expense);
    }

    private Expense getExpenseForAdmin(Long societyId, Long expenseId) {
        return expenseRepository.findByIdAndSocietyId(expenseId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Expense", expenseId));
    }

    private void validateProofFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw SocietyLedgerException.badRequest("Proof file cannot be empty.");
        }
        if (file.getSize() > MAX_PROOF_SIZE_BYTES) {
            throw SocietyLedgerException.badRequest("Proof file size cannot exceed 10 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_PROOF_TYPES.contains(contentType)) {
            throw SocietyLedgerException.badRequest(
                    "Proof file must be a PDF, JPEG, PNG, or WebP image.");
        }
    }

    private String saveProofFile(Long societyId, Long expenseId, MultipartFile file) {
        try {
            Path dir = Paths.get(PROOF_DIR + societyId + "/");
            Files.createDirectories(dir);
            String uniqueName = expenseId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(uniqueName);
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException e) {
            throw new SocietyLedgerException("Failed to save proof file.", "FILE_STORAGE_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ExpenseResponse mapToResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .societyId(e.getSocietyId())
                .categoryId(e.getCategory().getId())
                .categoryName(e.getCategory().getName())
                .amount(e.getAmount())
                .vendorName(e.getVendorName())
                .description(e.getDescription())
                .expenseDate(e.getExpenseDate())
                .status(e.getStatus().name())
                .hasProof(e.hasProof())
                .proofFileName(e.getProofFileName())
                .publishedAt(e.getPublishedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
