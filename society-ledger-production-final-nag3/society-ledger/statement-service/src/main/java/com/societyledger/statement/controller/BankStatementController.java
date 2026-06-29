package com.societyledger.statement.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.statement.dto.response.BankStatementResponse;
import com.societyledger.statement.dto.response.PaymentRecordResponse;
import com.societyledger.statement.dto.response.TransactionResponse;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.entity.PaymentRecord;
import com.societyledger.statement.repository.BankTransactionRepository;
import com.societyledger.statement.repository.PaymentRecordRepository;
import com.societyledger.statement.repository.UnmatchedTransactionRepository;
import com.societyledger.statement.service.PaymentMatchingService;
import com.societyledger.statement.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/statements/{societyId}")
@RequiredArgsConstructor
public class BankStatementController {

    private final StatementService statementService;
    private final PaymentMatchingService matchingService;
    private final BankTransactionRepository transactionRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final UnmatchedTransactionRepository unmatchedRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankStatementResponse>> upload(
            @PathVariable Long societyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String bankCode,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.uploadStatement(societyId, file, bankCode, month, year,
                        claims.getUserId()),
                "Statement uploaded and processing started."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BankStatementResponse>>> getStatements(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.getStatements(societyId,
                        PageRequest.of(page, size, Sort.by("uploadedAt").descending()))));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @PathVariable Long societyId,
            @RequestParam(required = false) String matchStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TransactionResponse> result;
        if (matchStatus != null) {
            BankTransaction.MatchStatus status = BankTransaction.MatchStatus.valueOf(matchStatus.toUpperCase());
            result = PageResponse.from(
                    transactionRepository.findBySocietyIdAndMatchStatus(societyId, status,
                            PageRequest.of(page, size, Sort.by("transactionDate").descending()))
                            .map(this::mapTxnToResponse));
        } else {
            result = PageResponse.from(
                    transactionRepository.findBySocietyIdOrderByTransactionDateDesc(societyId,
                            PageRequest.of(page, size))
                            .map(this::mapTxnToResponse));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/transactions/unmatched")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getUnmatched(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TransactionResponse> result = PageResponse.from(
                transactionRepository.findBySocietyIdAndMatchStatus(
                        societyId, BankTransaction.MatchStatus.UNMATCHED,
                        PageRequest.of(page, size, Sort.by("transactionDate").descending()))
                        .map(this::mapTxnToResponse));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/transactions/{transactionId}/manual-match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentRecordResponse>> manualMatch(
            @PathVariable Long societyId,
            @PathVariable Long transactionId,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal JwtClaims claims) {
        Long flatId = body.get("flatId");
        PaymentRecord record = matchingService.manualMatch(
                societyId, transactionId, flatId, claims.getUserId());
        return ResponseEntity.ok(ApiResponse.success(mapPaymentToResponse(record),
                "Transaction manually matched and receipt generation triggered."));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PageResponse<PaymentRecordResponse>>> getPayments(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                paymentRecordRepository.findBySocietyIdOrderByPaymentDateDesc(societyId,
                        PageRequest.of(page, size))
                        .map(this::mapPaymentToResponse))));
    }

    @GetMapping("/payments/flat/{flatId}")
    public ResponseEntity<ApiResponse<PageResponse<PaymentRecordResponse>>> getPaymentsByFlat(
            @PathVariable Long societyId,
            @PathVariable Long flatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {
        if ("RESIDENT".equals(claims.getRole()) && !claims.getFlatId().equals(flatId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied.", "FORBIDDEN"));
        }
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                paymentRecordRepository.findBySocietyIdAndFlatIdOrderByPaymentDateDesc(
                        societyId, flatId, PageRequest.of(page, size))
                        .map(this::mapPaymentToResponse))));
    }

    private TransactionResponse mapTxnToResponse(BankTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).societyId(t.getSocietyId())
                .transactionDate(t.getTransactionDate()).description(t.getDescription())
                .referenceNumber(t.getReferenceNumber())
                .creditAmount(t.getCreditAmount()).debitAmount(t.getDebitAmount())
                .balance(t.getBalance()).matchStatus(t.getMatchStatus().name())
                .build();
    }

    private PaymentRecordResponse mapPaymentToResponse(PaymentRecord p) {
        return PaymentRecordResponse.builder()
                .id(p.getId()).societyId(p.getSocietyId()).flatId(p.getFlatId())
                .paymentMonth(p.getPaymentMonth()).paymentYear(p.getPaymentYear())
                .amount(p.getAmount()).paymentDate(p.getPaymentDate())
                .paymentReference(p.getPaymentReference())
                .matchType(p.getMatchType().name()).createdAt(p.getCreatedAt())
                .build();
    }
}
