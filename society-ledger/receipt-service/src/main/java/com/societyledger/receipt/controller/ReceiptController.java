package com.societyledger.receipt.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.receipt.dto.response.ReceiptResponse;
import com.societyledger.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts/{societyId}")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /** Admin: all receipts for the society */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReceiptResponse>>> getAllReceipts(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                receiptService.getAllReceipts(societyId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    /** Resident: own flat receipts */
    @GetMapping("/my")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<ReceiptResponse>>> getMyReceipts(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                receiptService.getReceiptsForFlat(societyId, claims.getFlatId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    /** Admin: receipts for a specific flat */
    @GetMapping("/flat/{flatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReceiptResponse>>> getFlatReceipts(
            @PathVariable Long societyId, @PathVariable Long flatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                receiptService.getReceiptsForFlat(societyId, flatId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getById(
            @PathVariable Long societyId, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.getById(societyId, id)));
    }
}
