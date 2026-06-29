package com.societyledger.receipt.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.receipt.dto.response.ReceiptResponse;
import com.societyledger.receipt.entity.Receipt;
import com.societyledger.receipt.kafka.ReceiptEventProducer;
import com.societyledger.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptEventProducer eventProducer;

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public void generateReceipt(Long societyId, Long flatId, Long transactionId,
                                 BigDecimal amount, String transactionDate, String refCode) {
        if (receiptRepository.existsByTransactionId(transactionId)) {
            log.warn("Receipt already exists for transaction {}", transactionId);
            return;
        }

        LocalDate txDate = parseTxDate(transactionDate);
        String receiptNo = buildReceiptNumber(societyId, txDate);

        Receipt saved = receiptRepository.save(Receipt.builder()
                .societyId(societyId).flatId(flatId).transactionId(transactionId)
                .receiptNumber(receiptNo).amount(amount)
                .transactionDate(transactionDate).referenceCode(refCode)
                .month(txDate.getMonthValue()).year(txDate.getYear())
                .status(Receipt.ReceiptStatus.GENERATED)
                .build());

        eventProducer.publishReceiptGenerated(societyId, saved.getId(), flatId, amount);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptResponse> getReceiptsForFlat(Long societyId, Long flatId, Pageable p) {
        return PageResponse.from(
                receiptRepository.findBySocietyIdAndFlatIdOrderByCreatedAtDesc(societyId, flatId, p)
                                 .map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptResponse> getAllReceipts(Long societyId, Pageable p) {
        return PageResponse.from(
                receiptRepository.findBySocietyIdOrderByCreatedAtDesc(societyId, p)
                                 .map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getById(Long societyId, Long id) {
        return receiptRepository.findByIdAndSocietyId(id, societyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("Receipt", id));
    }

    private String buildReceiptNumber(Long societyId, LocalDate date) {
        return String.format("SL-%d-%s-%05d",
                societyId,
                date.format(DateTimeFormatter.ofPattern("yyyyMM")),
                SEQ.incrementAndGet() % 100000);
    }

    private LocalDate parseTxDate(String dateStr) {
        try { return LocalDate.parse(dateStr); }
        catch (Exception e) { return LocalDate.now(); }
    }

    private ReceiptResponse mapToResponse(Receipt r) {
        return ReceiptResponse.builder()
                .id(r.getId()).societyId(r.getSocietyId()).flatId(r.getFlatId())
                .transactionId(r.getTransactionId()).receiptNumber(r.getReceiptNumber())
                .amount(r.getAmount()).transactionDate(r.getTransactionDate())
                .referenceCode(r.getReferenceCode()).flatNumber(r.getFlatNumber())
                .wingName(r.getWingName()).month(r.getMonth()).year(r.getYear())
                .status(r.getStatus()).createdAt(r.getCreatedAt())
                .build();
    }
}
