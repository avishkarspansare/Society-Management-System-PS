package com.societyledger.statement.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.statement.dto.response.TransactionResponse;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.feign.SocietyServiceClient;
import com.societyledger.statement.kafka.StatementEventProducer;
import com.societyledger.statement.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMatchingService {

    private final BankTransactionRepository transactionRepository;
    private final SocietyServiceClient societyServiceClient;
    private final StatementEventProducer eventProducer;

    @Async
    @Transactional
    public void matchAll(Long societyId, Long statementId) {
        log.info("Starting payment matching for statement {} society {}", statementId, societyId);
        List<BankTransaction> unmatched = transactionRepository
                .findByStatementIdAndMatchStatus(statementId, BankTransaction.MatchStatus.UNMATCHED);
        if (unmatched.isEmpty()) {
            log.info("No unmatched transactions for statement {}", statementId);
            return;
        }

        // Fetch all flat payment refs once
        List<Map<String, Object>> flatRefs;
        try {
            var resp = societyServiceClient.getAllFlatPaymentRefs(societyId);
            if (resp.getBody() == null || resp.getBody().getData() == null) {
                log.warn("No flat payment refs found for society {}", societyId);
                return;
            }
            // We get ApiResponse<List<FlatPaymentRefResponse>>; cast to raw list
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> refs = (List<Map<String, Object>>) resp.getBody().getData();
            flatRefs = refs;
        } catch (Exception e) {
            log.error("Failed to fetch flat refs for matching: {}", e.getMessage());
            return;
        }

        int matched = 0;
        for (BankTransaction txn : unmatched) {
            Long flatId = findMatchingFlat(txn, flatRefs);
            if (flatId != null) {
                txn.setMatchStatus(BankTransaction.MatchStatus.AUTO_MATCHED);
                txn.setMatchedFlatId(flatId);
                txn.setMatchedAt(Instant.now());
                transactionRepository.save(txn);
                eventProducer.publishPaymentMatched(societyId, flatId, txn.getId(),
                        txn.getAmount(), txn.getTransactionDate().toString(), txn.getReferenceCode());
                matched++;
            } else {
                eventProducer.publishPaymentUnmatched(societyId, txn.getId(),
                        txn.getAmount(), txn.getTransactionDate().toString());
            }
        }
        log.info("Matching complete: {}/{} matched for statement {}", matched, unmatched.size(), statementId);
    }

    @Transactional
    public TransactionResponse manualMatch(Long societyId, Long txnId, Long flatId, Long adminUserId) {
        BankTransaction txn = transactionRepository.findByIdAndSocietyId(txnId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Transaction", txnId));
        if (txn.getMatchStatus() == BankTransaction.MatchStatus.AUTO_MATCHED
                || txn.getMatchStatus() == BankTransaction.MatchStatus.MANUALLY_MATCHED) {
            throw SocietyLedgerException.badRequest("Transaction is already matched.", "ALREADY_MATCHED");
        }
        txn.setMatchStatus(BankTransaction.MatchStatus.MANUALLY_MATCHED);
        txn.setMatchedFlatId(flatId);
        txn.setMatchedBy(adminUserId);
        txn.setMatchedAt(Instant.now());
        BankTransaction saved = transactionRepository.save(txn);

        eventProducer.publishPaymentMatched(societyId, flatId, txnId,
                txn.getAmount(), txn.getTransactionDate().toString(), txn.getReferenceCode());

        return mapTxnToResponse(saved);
    }

    /**
     * Matching priority:
     * 1. Reference code exact match against flat payment reference code
     * 2. Partial reference match (flat ref contained in txn description)
     */
    private Long findMatchingFlat(BankTransaction txn, List<Map<String, Object>> flatRefs) {
        if (txn.getReferenceCode() != null && !txn.getReferenceCode().isBlank()) {
            String ref = txn.getReferenceCode().toUpperCase();
            for (Map<String, Object> flat : flatRefs) {
                String payRef = Objects.toString(flat.get("paymentReferenceCode"), "").toUpperCase();
                if (ref.contains(payRef) || payRef.equals(ref)) {
                    return toLong(flat.get("flatId"));
                }
            }
        }
        // Fallback: scan description
        if (txn.getDescription() != null) {
            String desc = txn.getDescription().toUpperCase();
            for (Map<String, Object> flat : flatRefs) {
                String payRef = Objects.toString(flat.get("paymentReferenceCode"), "").toUpperCase();
                if (desc.contains(payRef)) {
                    return toLong(flat.get("flatId"));
                }
            }
        }
        return null;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }

    private TransactionResponse mapTxnToResponse(BankTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).statementId(t.getStatementId()).societyId(t.getSocietyId())
                .transactionDate(t.getTransactionDate()).description(t.getDescription())
                .amount(t.getAmount()).referenceCode(t.getReferenceCode())
                .matchStatus(t.getMatchStatus()).matchedFlatId(t.getMatchedFlatId())
                .build();
    }
}
