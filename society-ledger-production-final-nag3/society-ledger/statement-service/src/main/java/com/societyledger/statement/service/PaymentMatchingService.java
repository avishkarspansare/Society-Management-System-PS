package com.societyledger.statement.service;

import com.societyledger.statement.dto.FlatPaymentRefDto;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.entity.PaymentRecord;
import com.societyledger.statement.entity.UnmatchedTransaction;
import com.societyledger.statement.feign.SocietyServiceClient;
import com.societyledger.statement.kafka.ReceiptEventProducer;
import com.societyledger.statement.repository.BankTransactionRepository;
import com.societyledger.statement.repository.PaymentRecordRepository;
import com.societyledger.statement.repository.UnmatchedTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Payment Matching Engine.
 *
 * Matching Flow:
 * 1. Reference Match   — find flat by payment_reference_code in transaction description/ref
 * 2. Amount Match      — credit amount must be > 0 (it's an income)
 * 3. Date Match        — derive payment month/year from transaction date
 * 4. Duplicate Check   — no double payments for same flat+month+year
 * 5. Result            — AUTO matched → PaymentRecord created → Kafka event
 *                        UNMATCHED     → UnmatchedTransaction created for manual resolution
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMatchingService {

    private final BankTransactionRepository transactionRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final UnmatchedTransactionRepository unmatchedTransactionRepository;
    private final SocietyServiceClient societyServiceClient;
    private final ReceiptEventProducer receiptEventProducer;

    /**
     * Process all unmatched credit transactions for a society and attempt auto-matching.
     * Called after a bank statement is fully parsed and saved.
     */
    @Transactional
    public void matchTransactionsForSociety(Long societyId, Long statementId) {
        // Load all flat payment references for this society once — avoids N+1 Feign calls
        List<FlatPaymentRefDto> flatRefs = societyServiceClient.getAllFlatPaymentRefs(societyId);
        Map<String, FlatPaymentRefDto> refCodeToFlat = flatRefs.stream()
                .collect(Collectors.toUnmodifiableMap(
                        f -> f.getPaymentReferenceCode().toUpperCase(),
                        f -> f
                ));

        // Fetch all unmatched CREDIT transactions for this statement
        List<BankTransaction> unmatchedCredits = transactionRepository
                .findByStatementIdAndMatchStatus(statementId, BankTransaction.MatchStatus.UNMATCHED)
                .stream()
                .filter(BankTransaction::isCredit)
                .toList();

        log.info("Attempting to match {} credit transactions for society {} statement {}",
                unmatchedCredits.size(), societyId, statementId);

        // Stream-process each transaction
        unmatchedCredits.forEach(txn -> matchSingleTransaction(txn, refCodeToFlat));
    }

    private void matchSingleTransaction(BankTransaction txn,
                                        Map<String, FlatPaymentRefDto> refCodeToFlat) {
        // Step 1: Reference Match
        Optional<FlatPaymentRefDto> matchedFlat = findFlatByReference(txn, refCodeToFlat);

        if (matchedFlat.isEmpty()) {
            markUnmatched(txn, "No flat found matching reference in: " + txn.getDescription());
            return;
        }

        FlatPaymentRefDto flat = matchedFlat.get();

        // Step 2: Amount validation (credit must be positive)
        if (txn.getCreditAmount().compareTo(BigDecimal.ZERO) <= 0) {
            markUnmatched(txn, "Credit amount is zero or negative.");
            return;
        }

        // Step 3: Duplicate check — same flat + month + year
        int paymentMonth = txn.getTransactionDate().getMonthValue();
        int paymentYear  = txn.getTransactionDate().getYear();

        if (paymentRecordRepository.existsByFlatIdAndPaymentYearAndPaymentMonthAndPaymentType(
                flat.getFlatId(), paymentYear, paymentMonth, "MAINTENANCE")) {
            markUnmatched(txn, "Payment already recorded for flat " + flat.getPaymentReferenceCode()
                    + " for " + paymentMonth + "/" + paymentYear);
            return;
        }

        // Auto-match success — create payment record
        PaymentRecord record = PaymentRecord.builder()
                .societyId(txn.getSocietyId())
                .flatId(flat.getFlatId())
                .transaction(txn)
                .paymentMonth(paymentMonth)
                .paymentYear(paymentYear)
                .amount(txn.getCreditAmount())
                .paymentDate(txn.getTransactionDate())
                .paymentReference(txn.getReferenceNumber())
                .paymentType("MAINTENANCE")
                .matchType(PaymentRecord.MatchType.AUTO)
                .build();

        PaymentRecord saved = paymentRecordRepository.save(record);

        // Update transaction match status
        txn.setMatchStatus(BankTransaction.MatchStatus.MATCHED);
        transactionRepository.save(txn);

        log.info("AUTO-MATCHED: txn {} → flat {} ({})", txn.getId(),
                flat.getFlatId(), flat.getPaymentReferenceCode());

        // Publish Kafka event → triggers receipt generation
        receiptEventProducer.publishPaymentMatched(saved, flat);
    }

    /**
     * Manual match — admin maps an unmatched transaction to a specific flat.
     */
    @Transactional
    public PaymentRecord manualMatch(Long societyId, Long transactionId,
                                     Long flatId, Long adminUserId) {
        BankTransaction txn = transactionRepository
                .findByIdAndSocietyId(transactionId, societyId)
                .orElseThrow(() -> new com.societyledger.common.exception.SocietyLedgerException(
                        "Transaction not found.", "TXN_NOT_FOUND",
                        org.springframework.http.HttpStatus.NOT_FOUND));

        FlatPaymentRefDto flat = societyServiceClient.getFlatPaymentRef(societyId, flatId);

        int paymentMonth = txn.getTransactionDate().getMonthValue();
        int paymentYear  = txn.getTransactionDate().getYear();

        PaymentRecord record = PaymentRecord.builder()
                .societyId(societyId)
                .flatId(flatId)
                .transaction(txn)
                .paymentMonth(paymentMonth)
                .paymentYear(paymentYear)
                .amount(txn.getCreditAmount())
                .paymentDate(txn.getTransactionDate())
                .paymentReference(txn.getReferenceNumber())
                .paymentType("MAINTENANCE")
                .matchType(PaymentRecord.MatchType.MANUAL)
                .build();

        PaymentRecord saved = paymentRecordRepository.save(record);

        txn.setMatchStatus(BankTransaction.MatchStatus.MANUALLY_MATCHED);
        transactionRepository.save(txn);

        // Resolve unmatched record if present
        unmatchedTransactionRepository.findByTransactionId(transactionId)
                .ifPresent(um -> {
                    um.setResolved(true);
                    um.setResolvedBy(adminUserId);
                    um.setResolvedAt(java.time.Instant.now());
                    unmatchedTransactionRepository.save(um);
                });

        log.info("MANUAL-MATCHED: txn {} → flat {} by admin {}", transactionId, flatId, adminUserId);

        // Also trigger receipt generation for manual matches
        receiptEventProducer.publishPaymentMatched(saved, flat);

        return saved;
    }

    private Optional<FlatPaymentRefDto> findFlatByReference(BankTransaction txn,
                                                              Map<String, FlatPaymentRefDto> refCodeToFlat) {
        // Try reference number field first
        if (txn.getReferenceNumber() != null) {
            String refUpper = txn.getReferenceNumber().toUpperCase();
            if (refCodeToFlat.containsKey(refUpper)) {
                return Optional.of(refCodeToFlat.get(refUpper));
            }
        }

        // Try to find any known reference code within the description
        if (txn.getDescription() != null) {
            String descUpper = txn.getDescription().toUpperCase();
            return refCodeToFlat.entrySet().stream()
                    .filter(entry -> descUpper.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst();
        }

        return Optional.empty();
    }

    private void markUnmatched(BankTransaction txn, String reason) {
        txn.setMatchStatus(BankTransaction.MatchStatus.UNMATCHED);
        transactionRepository.save(txn);

        UnmatchedTransaction unmatched = UnmatchedTransaction.builder()
                .transaction(txn)
                .societyId(txn.getSocietyId())
                .reason(reason)
                .build();
        unmatchedTransactionRepository.save(unmatched);

        log.warn("UNMATCHED: txn {} — reason: {}", txn.getId(), reason);
    }
}
