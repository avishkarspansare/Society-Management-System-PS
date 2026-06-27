package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateWingRequest;
import com.societyledger.society.dto.response.WingResponse;
import com.societyledger.society.service.WingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/societies/{societyId}/wings")
@RequiredArgsConstructor
public class WingController {

    private final WingService wingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WingResponse>> createWing(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateWingRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                wingService.createWing(societyId, request, claims.getUserId()),
                "Wing created successfully."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<WingResponse>>> getWings(@PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(wingService.getWings(societyId)));
    }
}
