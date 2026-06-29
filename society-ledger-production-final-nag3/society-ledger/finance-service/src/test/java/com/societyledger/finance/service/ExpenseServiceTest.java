package com.societyledger.finance.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.dto.request.CreateExpenseRequest;
import com.societyledger.finance.dto.response.ExpenseResponse;
import com.societyledger.finance.entity.Expense;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.kafka.ExpenseEventProducer;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import com.societyledger.finance.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Tests")
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepository;
    @Mock ExpenseCategoryRepository categoryRepository;
    @Mock ExpenseEventProducer eventProducer;
    @Mock TimelineService timelineService;

    @InjectMocks ExpenseService expenseService;

    private static final Long SOCIETY_ID = 1L;
    private static final Long ADMIN_ID    = 99L;
    private static final Long CATEGORY_ID = 5L;

    private ExpenseCategory category;
    private Expense draftExpense;

    @BeforeEach
    void setUp() {
        category = ExpenseCategory.builder()
                .id(CATEGORY_ID).societyId(SOCIETY_ID).name("Maintenance").build();

        draftExpense = Expense.builder()
                .id(1L).societyId(SOCIETY_ID).category(category)
                .amount(new BigDecimal("5000")).vendorName("ABC Plumbing")
                .description("Pipe repair").expenseDate(LocalDate.now())
                .status(Expense.ExpenseStatus.DRAFT).createdBy(ADMIN_ID)
                .build();
    }

    @Test
    @DisplayName("createExpense saves and returns a DRAFT expense")
    void testCreateExpense() {
        when(categoryRepository.findByIdAndSocietyId(CATEGORY_ID, SOCIETY_ID))
                .thenReturn(Optional.of(category));
        when(expenseRepository.save(any())).thenReturn(draftExpense);

        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setCategoryId(CATEGORY_ID);
        req.setAmount(new BigDecimal("5000"));
        req.setVendorName("ABC Plumbing");
        req.setDescription("Pipe repair");
        req.setExpenseDate(LocalDate.now());

        ExpenseResponse response = expenseService.createExpense(SOCIETY_ID, req, ADMIN_ID);

        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getVendorName()).isEqualTo("ABC Plumbing");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("publishExpense without proof throws PROOF_REQUIRED")
    void testPublishWithoutProofThrows() {
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));
        // draftExpense has no proof

        assertThatThrownBy(() -> expenseService.publishExpense(SOCIETY_ID, 1L, ADMIN_ID))
                .isInstanceOf(SocietyLedgerException.class)
                .satisfies(e -> assertThat(((SocietyLedgerException) e).getErrorCode())
                        .isEqualTo("PROOF_REQUIRED"));
    }

    @Test
    @DisplayName("publishExpense with proof transitions to PUBLISHED and fires Kafka event")
    void testPublishWithProofSucceeds() {
        draftExpense.setProofFilePath("/some/path/proof.pdf");
        draftExpense.setProofFileName("proof.pdf");

        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));
        when(expenseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExpenseResponse response = expenseService.publishExpense(SOCIETY_ID, 1L, ADMIN_ID);

        assertThat(response.getStatus()).isEqualTo("PUBLISHED");
        verify(eventProducer).publishExpensePublished(any());
        verify(timelineService).record(eq(SOCIETY_ID), eq("EXPENSE_PUBLISHED"),
                eq(ADMIN_ID), anyString(), eq("EXPENSE"), eq(1L));
    }

    @Test
    @DisplayName("publishExpense on already-published expense throws BAD_REQUEST")
    void testPublishAlreadyPublishedThrows() {
        draftExpense.setStatus(Expense.ExpenseStatus.PUBLISHED);
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));

        assertThatThrownBy(() -> expenseService.publishExpense(SOCIETY_ID, 1L, ADMIN_ID))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("Only DRAFT");
    }

    @Test
    @DisplayName("archiveExpense transitions PUBLISHED → ARCHIVED")
    void testArchiveExpense() {
        draftExpense.setStatus(Expense.ExpenseStatus.PUBLISHED);
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));
        when(expenseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExpenseResponse response = expenseService.archiveExpense(SOCIETY_ID, 1L, ADMIN_ID);

        assertThat(response.getStatus()).isEqualTo("ARCHIVED");
    }

    @Test
    @DisplayName("RESIDENT can only see PUBLISHED expenses")
    void testResidentSeesOnlyPublished() {
        draftExpense.setStatus(Expense.ExpenseStatus.DRAFT);
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));

        assertThatThrownBy(() -> expenseService.getExpenseById(SOCIETY_ID, 1L, "RESIDENT"))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("ADMIN can see DRAFT expenses")
    void testAdminSeesDraft() {
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));

        ExpenseResponse response = expenseService.getExpenseById(SOCIETY_ID, 1L, "ADMIN");

        assertThat(response.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("uploadProof rejects file over 10MB")
    void testUploadProofRejectsLargeFile() {
        when(expenseRepository.findByIdAndSocietyId(1L, SOCIETY_ID))
                .thenReturn(Optional.of(draftExpense));

        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", largeContent);

        assertThatThrownBy(() -> expenseService.uploadProof(SOCIETY_ID, 1L, bigFile, ADMIN_ID))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("10 MB");
    }

    @Test
    @DisplayName("createExpense with unknown category throws NOT_FOUND")
    void testCreateWithUnknownCategoryThrows() {
        when(categoryRepository.findByIdAndSocietyId(999L, SOCIETY_ID))
                .thenReturn(Optional.empty());

        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setCategoryId(999L);
        req.setAmount(BigDecimal.TEN);
        req.setVendorName("X");
        req.setDescription("Y");
        req.setExpenseDate(LocalDate.now());

        assertThatThrownBy(() -> expenseService.createExpense(SOCIETY_ID, req, ADMIN_ID))
                .isInstanceOf(SocietyLedgerException.class);
    }
}
