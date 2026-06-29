package com.societyledger.receipt.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.receipt.dto.response.ReceiptResponse;
import com.societyledger.receipt.entity.Receipt;
import com.societyledger.receipt.feign.SocietyServiceClient;
import com.societyledger.receipt.feign.StatementServiceClient;
import com.societyledger.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PdfGenerationService pdfGenerationService;
    private final SocietyServiceClient societyServiceClient;
    private final StatementServiceClient statementServiceClient;

    /**
     * Called by Kafka consumer after RECEIPT_GENERATED event.
     * Creates the Receipt record and generates the PDF.
     */
    @Transactional
    public ReceiptResponse createReceipt(Long societyId, Long flatId, Long paymentId,
                                          BigDecimal amount, Integer paymentMonth, Integer paymentYear) {
        // Avoid duplicate receipts
        if (receiptRepository.existsByPaymentId(paymentId)) {
            log.warn("Receipt already exists for paymentId {}, skipping.", paymentId);
            return mapToResponse(receiptRepository.findByPaymentId(paymentId).get());
        }

        // Fetch flat and society details via Feign (synchronous, needed for PDF)
        var flatDetails    = societyServiceClient.getFlatDetails(societyId, flatId);
        var societyDetails = societyServiceClient.getSocietyDetails(societyId);

        String receiptNumber = generateReceiptNumber(societyId);

        Receipt receipt = Receipt.builder()
                .societyId(societyId)
                .flatId(flatId)
                .paymentId(paymentId)
                .receiptNumber(receiptNumber)
                .amount(amount)
                .paymentMonth(paymentMonth)
                .paymentYear(paymentYear)
                .residentName(flatDetails.getPrimaryResidentName())
                .flatNumber(flatDetails.getFlatNumber())
                .societyName(societyDetails.getSocietyName())
                .build();

        // Generate PDF and store path
        String pdfPath = pdfGenerationService.generateReceiptPdf(receipt);
        receipt.setPdfFilePath(pdfPath);

        Receipt saved = receiptRepository.save(receipt);
        log.info("Receipt created: {} for flat {} payment {}", receiptNumber, flatId, paymentId);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptResponse> getReceiptsForSociety(Long societyId, Pageable pageable) {
        return PageResponse.from(
                receiptRepository.findBySocietyIdOrderByGeneratedAtDesc(societyId, pageable)
                        .map(this::mapToResponse)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptResponse> getReceiptsForFlat(Long societyId, Long flatId,
                                                              Long requestingFlatId, String role,
                                                              Pageable pageable) {
        // Residents can only see their own flat's receipts
        if ("RESIDENT".equals(role) && !requestingFlatId.equals(flatId)) {
            throw SocietyLedgerException.forbidden("You can only view your own receipts.");
        }
        return PageResponse.from(
                receiptRepository.findBySocietyIdAndFlatIdOrderByGeneratedAtDesc(
                        societyId, flatId, pageable).map(this::mapToResponse)
        );
    }

    /**
     * Returns PDF bytes for download. Regenerates from DB data if file not found.
     */
    @Transactional(readOnly = true)
    public byte[] downloadReceiptPdf(Long societyId, Long receiptId,
                                      Long requestingFlatId, String role) {
        Receipt receipt = receiptRepository.findByIdAndSocietyId(receiptId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Receipt", receiptId));

        // Residents can only download their own receipts
        if ("RESIDENT".equals(role) && !receipt.getFlatId().equals(requestingFlatId)) {
            throw SocietyLedgerException.forbidden("You can only download your own receipts.");
        }

        // Try reading from disk first
        if (receipt.getPdfFilePath() != null) {
            Path pdfPath = Paths.get(receipt.getPdfFilePath());
            if (Files.exists(pdfPath)) {
                try {
                    return Files.readAllBytes(pdfPath);
                } catch (Exception e) {
                    log.warn("Could not read PDF from disk, regenerating: {}", e.getMessage());
                }
            }
        }

        // Regenerate PDF on-the-fly if file missing
        return pdfGenerationService.generateReceiptPdfBytes(receipt);
    }

    /**
     * Generates receipt number like: RCP-2024-000001
     * Uses DB count to derive sequence — deterministic, no separate sequence table needed.
     */
    private String generateReceiptNumber(Long societyId) {
        long count = receiptRepository.countBySocietyId(societyId) + 1;
        return String.format("RCP-%d-%06d", Year.now().getValue(), count);
    }

    private ReceiptResponse mapToResponse(Receipt r) {
        return ReceiptResponse.builder()
                .id(r.getId())
                .societyId(r.getSocietyId())
                .flatId(r.getFlatId())
                .paymentId(r.getPaymentId())
                .receiptNumber(r.getReceiptNumber())
                .amount(r.getAmount())
                .paymentMonth(r.getPaymentMonth())
                .paymentYear(r.getPaymentYear())
                .residentName(r.getResidentName())
                .flatNumber(r.getFlatNumber())
                .societyName(r.getSocietyName())
                .generatedAt(r.getGeneratedAt())
                .hasPdf(r.getPdfFilePath() != null)
                .build();
    }
}
