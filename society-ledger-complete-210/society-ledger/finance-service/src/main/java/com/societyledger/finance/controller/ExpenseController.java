package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.finance.dto.request.CreateExpenseRequest;
import com.societyledger.finance.dto.response.ExpenseResponse;
import com.societyledger.finance.service.ExpenseService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/finance/{societyId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.createExpense(societyId, request, claims.getUserId()),
                "Expense created in DRAFT."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getExpenses(
            @PathVariable Long societyId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.getExpenses(societyId, status, claims.getRole(),
                        PageRequest.of(page, size, Sort.by("expenseDate").descending()))));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(
            @PathVariable Long societyId, @PathVariable Long expenseId,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.getExpenseById(societyId, expenseId, claims.getRole())));
    }

    @PostMapping(value = "/{expenseId}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> uploadProof(
            @PathVariable Long societyId, @PathVariable Long expenseId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.uploadProof(societyId, expenseId, file, claims.getUserId()),
                "Proof uploaded successfully."));
    }

    @PatchMapping("/{expenseId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> publish(
            @PathVariable Long societyId, @PathVariable Long expenseId,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.publishExpense(societyId, expenseId, claims.getUserId()),
                "Expense published successfully."));
    }

    @PatchMapping("/{expenseId}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> archive(
            @PathVariable Long societyId, @PathVariable Long expenseId,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                expenseService.archiveExpense(societyId, expenseId, claims.getUserId()),
                "Expense archived."));
    }
}
