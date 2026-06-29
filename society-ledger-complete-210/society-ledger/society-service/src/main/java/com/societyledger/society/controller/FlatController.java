package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateFlatRequest;
import com.societyledger.society.dto.response.FlatPaymentRefResponse;
import com.societyledger.society.dto.response.FlatResponse;
import com.societyledger.society.service.FlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/societies/{societyId}/flats")
@RequiredArgsConstructor
public class FlatController {

    private final FlatService flatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlatResponse>> createFlat(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateFlatRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                flatService.createFlat(societyId, request, claims.getUserId()),
                "Flat created successfully."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<FlatResponse>>> getFlats(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(flatService.getFlats(societyId,
                PageRequest.of(page, size, Sort.by("flatNumber")))));
    }

    @GetMapping("/{flatId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<FlatResponse>> getFlatById(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        return ResponseEntity.ok(ApiResponse.success(flatService.getFlatById(societyId, flatId)));
    }

    /** Internal endpoint used by other services via Feign — validates flat existence. */
    @GetMapping("/{flatId}/validate")
    public ResponseEntity<Void> validateFlat(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        flatService.validateFlatExists(societyId, flatId);
        return ResponseEntity.ok().build();
    }

    /** Internal endpoint: get all flat payment refs for matching engine. */
    @GetMapping("/payment-refs")
    public ResponseEntity<ApiResponse<List<FlatPaymentRefResponse>>> getAllPaymentRefs(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(flatService.getAllFlatPaymentRefs(societyId)));
    }

    /** Internal endpoint: get single flat payment ref. */
    @GetMapping("/{flatId}/payment-ref")
    public ResponseEntity<ApiResponse<FlatPaymentRefResponse>> getPaymentRef(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        return ResponseEntity.ok(ApiResponse.success(flatService.getFlatPaymentRefById(societyId, flatId)));
    }
}
