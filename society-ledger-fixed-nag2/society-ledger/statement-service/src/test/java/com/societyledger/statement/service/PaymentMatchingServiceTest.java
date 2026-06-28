package com.societyledger.statement.service;

import com.societyledger.statement.dto.FlatPaymentRefDto;
import com.societyledger.statement.entity.BankStatement;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.entity.PaymentRecord;
import com.societyledger.statement.feign.SocietyServiceClient;
import com.societyledger.statement.kafka.ReceiptEventProducer;
import com.societyledger.statement.repository.BankTransactionRepository;
import com.societyledger.statement.repository.PaymentRecordRepository;
import com.societyledger.statement.repository.UnmatchedTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMatchingService Tests")
class PaymentMatchingServiceTest {

    @Mock BankTransactionRepository transactionRepository;
    @Mock PaymentRecordRepository paymentRecordRepository;
    @Mock UnmatchedTransactionRepository unmatchedTransactionRepository;
    @Mock SocietyServiceClient societyServiceClient;
    @Mock ReceiptEventProducer receiptEventProducer;

    @InjectMocks PaymentMatchingService matchingService;

    private static final Long SOCIETY_ID = 1L;
    private static final Long STATEMENT_ID = 10L;
    private static final Long FLAT_ID = 5L;

    private FlatPaymentRefDto flatRef;

    @BeforeEach
    void setUp() {
        flatRef = FlatPaymentRefDto.builder()
                .flatId(FLAT_ID)
                .societyId(SOCIETY_ID)
                .flatNumber("403")
                .wingName("B")
                .paymentReferenceCode("B403-MNT")
                .primaryResidentName("John Doe")
                .email("john@example.com")
                .build();
    }

    private BankTransaction buildCreditTxn(String description, String refNo, BigDecimal credit) {
        BankStatement stmt = BankStatement.builder().id(STATEMENT_ID).societyId(SOCIETY_ID).build();
        return BankTransaction.builder()
                .id(100L)
                .statement(stmt)
                .societyId(SOCIETY_ID)
                .transactionDate(LocalDate.of(2024, 1, 15))
                .description(description)
                .referenceNumber(refNo)
                .creditAmount(credit)
                .debitAmount(BigDecimal.ZERO)
                .balance(new BigDecimal("50000"))
                .matchStatus(BankTransaction.MatchStatus.UNMATCHED)
                .build();
    }

    @Test
    @DisplayName("Credit txn with matching reference is auto-matched")
    void testAutoMatchByReference() {
        BankTransaction txn = buildCreditTxn("NEFT B403-MNT PAYMENT", "B403-MNT", new BigDecimal("2500"));

        when(societyServiceClient.getAllFlatPaymentRefs(SOCIETY_ID))
                .thenReturn(List.of(flatRef));
        when(transactionRepository.findByStatementIdAndMatchStatus(STATEMENT_ID,
                BankTransaction.MatchStatus.UNMATCHED))
                .thenReturn(List.of(txn));
        when(paymentRecordRepository.existsByFlatIdAndPaymentYearAndPaymentMonthAndPaymentType(
                FLAT_ID, 2024, 1, "MAINTENANCE"))
                .thenReturn(false);
        when(paymentRecordRepository.save(any())).thenAnswer(i -> {
            PaymentRecord pr = i.getArgument(0);
            pr = PaymentRecord.builder().id(99L).societyId(pr.getSocietyId())
                    .flatId(pr.getFlatId()).amount(pr.getAmount())
                    .paymentMonth(pr.getPaymentMonth()).paymentYear(pr.getPaymentYear())
                    .matchType(pr.getMatchType()).paymentDate(pr.getPaymentDate()).build();
            return pr;
        });
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        matchingService.matchTransactionsForSociety(SOCIETY_ID, STATEMENT_ID);

        ArgumentCaptor<PaymentRecord> captor = ArgumentCaptor.forClass(PaymentRecord.class);
        verify(paymentRecordRepository).save(captor.capture());
        PaymentRecord saved = captor.getValue();

        assertThat(saved.getFlatId()).isEqualTo(FLAT_ID);
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("2500"));
        assertThat(saved.getMatchType()).isEqualTo(PaymentRecord.MatchType.AUTO);
        assertThat(saved.getPaymentMonth()).isEqualTo(1);
        assertThat(saved.getPaymentYear()).isEqualTo(2024);
        verify(receiptEventProducer).publishPaymentMatched(any(), eq(flatRef));
    }

    @Test
    @DisplayName("Duplicate payment (same flat+month+year) is marked unmatched")
    void testDuplicatePaymentNotMatched() {
        BankTransaction txn = buildCreditTxn("NEFT B403-MNT", "B403-MNT", new BigDecimal("2500"));

        when(societyServiceClient.getAllFlatPaymentRefs(SOCIETY_ID)).thenReturn(List.of(flatRef));
        when(transactionRepository.findByStatementIdAndMatchStatus(STATEMENT_ID,
                BankTransaction.MatchStatus.UNMATCHED)).thenReturn(List.of(txn));
        when(paymentRecordRepository.existsByFlatIdAndPaymentYearAndPaymentMonthAndPaymentType(
                FLAT_ID, 2024, 1, "MAINTENANCE")).thenReturn(true); // already paid
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(unmatchedTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        matchingService.matchTransactionsForSociety(SOCIETY_ID, STATEMENT_ID);

        verify(paymentRecordRepository, never()).save(any());
        verify(receiptEventProducer, never()).publishPaymentMatched(any(), any());
        verify(unmatchedTransactionRepository).save(any());
    }

    @Test
    @DisplayName("Transaction with no matching reference is marked unmatched")
    void testNoReferenceMatchMarkedUnmatched() {
        BankTransaction txn = buildCreditTxn("ATM CASH DEPOSIT", "ATM12345", new BigDecimal("5000"));

        when(societyServiceClient.getAllFlatPaymentRefs(SOCIETY_ID)).thenReturn(List.of(flatRef));
        when(transactionRepository.findByStatementIdAndMatchStatus(STATEMENT_ID,
                BankTransaction.MatchStatus.UNMATCHED)).thenReturn(List.of(txn));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(unmatchedTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        matchingService.matchTransactionsForSociety(SOCIETY_ID, STATEMENT_ID);

        verify(paymentRecordRepository, never()).save(any());
        verify(unmatchedTransactionRepository).save(any());
    }

    @Test
    @DisplayName("Debit-only transactions are skipped by matcher")
    void testDebitTransactionSkipped() {
        BankTransaction debitTxn = buildCreditTxn("UTILITY PAYMENT", "B403-MNT", BigDecimal.ZERO);
        debitTxn.setDebitAmount(new BigDecimal("1000"));

        when(societyServiceClient.getAllFlatPaymentRefs(SOCIETY_ID)).thenReturn(List.of(flatRef));
        when(transactionRepository.findByStatementIdAndMatchStatus(STATEMENT_ID,
                BankTransaction.MatchStatus.UNMATCHED)).thenReturn(List.of(debitTxn));

        matchingService.matchTransactionsForSociety(SOCIETY_ID, STATEMENT_ID);

        // Debit is skipped entirely — no unmatched record either
        verify(paymentRecordRepository, never()).save(any());
        verify(unmatchedTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Empty flat list results in all transactions being unmatched")
    void testEmptyFlatListAllUnmatched() {
        BankTransaction txn = buildCreditTxn("NEFT B403-MNT", "B403-MNT", new BigDecimal("2500"));

        when(societyServiceClient.getAllFlatPaymentRefs(SOCIETY_ID)).thenReturn(List.of());
        when(transactionRepository.findByStatementIdAndMatchStatus(STATEMENT_ID,
                BankTransaction.MatchStatus.UNMATCHED)).thenReturn(List.of(txn));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(unmatchedTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        matchingService.matchTransactionsForSociety(SOCIETY_ID, STATEMENT_ID);

        verify(paymentRecordRepository, never()).save(any());
        verify(unmatchedTransactionRepository).save(any());
    }
}
