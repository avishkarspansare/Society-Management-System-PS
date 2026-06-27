package com.societyledger.statement.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.statement.dto.response.BankStatementResponse;
import com.societyledger.statement.dto.response.TransactionResponse;
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

@RestController
@RequestMapping("/api/v1/statements/{societyId}")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankStatementResponse>> upload(
            @PathVariable Long societyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bankCode", defaultValue = "BOB") String bankCode,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.uploadAndParse(societyId, file, bankCode, claims.getUserId()),
                "Statement uploaded and matching initiated."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BankStatementResponse>>> getStatements(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.getStatements(societyId,
                        PageRequest.of(page, size, Sort.by("uploadedAt").descending()))));
    }

    @GetMapping("/{statementId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @PathVariable Long societyId, @PathVariable Long statementId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String matchStatus) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.getTransactions(societyId, statementId, matchStatus,
                        PageRequest.of(page, size, Sort.by("transactionDate").descending()))));
    }

    @PatchMapping("/transactions/{txnId}/manual-match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> manualMatch(
            @PathVariable Long societyId, @PathVariable Long txnId,
            @RequestParam Long flatId,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.manualMatch(societyId, txnId, flatId, claims.getUserId()),
                "Transaction manually matched."));
    }

    /** Internal: income total for a given month (used by finance-service). */
    @GetMapping("/income")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getIncomeForMonth(
            @PathVariable Long societyId,
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.getTotalIncomeForMonth(societyId, year, month)));
    }

    /** Internal: paid flat count for a given month. */
    @GetMapping("/paid-flats")
    public ResponseEntity<ApiResponse<Integer>> getPaidFlatsForMonth(
            @PathVariable Long societyId,
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(
                statementService.getPaidFlatsCountForMonth(societyId, year, month)));
    }
}
