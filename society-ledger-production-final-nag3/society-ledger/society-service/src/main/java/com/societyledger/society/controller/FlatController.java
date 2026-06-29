package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateFlatRequest;
import com.societyledger.society.dto.response.FlatResponse;
import com.societyledger.society.service.FlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/societies/{societyId}/flats")
@RequiredArgsConstructor
public class FlatController {

    private final FlatService flatService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FlatResponse>>> getFlats(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                flatService.getFlats(societyId,
                        PageRequest.of(page, size, Sort.by("flatNumber")))));
    }

    @GetMapping("/{flatId}")
    public ResponseEntity<ApiResponse<FlatResponse>> getFlatById(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        return ResponseEntity.ok(ApiResponse.success(flatService.getFlatById(societyId, flatId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlatResponse>> createFlat(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateFlatRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        FlatResponse flat = flatService.createFlat(societyId, request, claims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(flat));
    }
}
