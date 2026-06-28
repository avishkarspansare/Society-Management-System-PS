package com.societyledger.receipt.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.receipt.dto.response.ReceiptResponse;
import com.societyledger.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts/{societyId}")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReceiptResponse>>> getReceiptsForSociety(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {

        PageResponse<ReceiptResponse> receipts = receiptService.getReceiptsForSociety(
                societyId,
                PageRequest.of(page, size, Sort.by("generatedAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @GetMapping("/flat/{flatId}")
    public ResponseEntity<ApiResponse<PageResponse<ReceiptResponse>>> getReceiptsForFlat(
            @PathVariable Long societyId,
            @PathVariable Long flatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {

        PageResponse<ReceiptResponse> receipts = receiptService.getReceiptsForFlat(
                societyId, flatId, claims.getFlatId(), claims.getRole(),
                PageRequest.of(page, size, Sort.by("generatedAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(receipts));
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceiptById(
            @PathVariable Long societyId,
            @PathVariable Long receiptId,
            @AuthenticationPrincipal JwtClaims claims) {

        // Access check happens in service
        byte[] pdfBytes = receiptService.downloadReceiptPdf(
                societyId, receiptId, claims.getFlatId(), claims.getRole());
        // Just return the receipt metadata here; /download for the file
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{receiptId}/download")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long societyId,
            @PathVariable Long receiptId,
            @AuthenticationPrincipal JwtClaims claims) {

        byte[] pdfBytes = receiptService.downloadReceiptPdf(
                societyId, receiptId, claims.getFlatId(), claims.getRole());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt-" + receiptId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
