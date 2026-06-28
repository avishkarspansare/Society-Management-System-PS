package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateWingRequest;
import com.societyledger.society.dto.response.SocietyResponse;
import com.societyledger.society.dto.response.WingResponse;
import com.societyledger.society.service.SocietyService;
import com.societyledger.society.service.WingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/societies/{societyId}")
@RequiredArgsConstructor
public class SocietyController {

    private final SocietyService societyService;
    private final WingService wingService;

    @GetMapping
    public ResponseEntity<ApiResponse<SocietyResponse>> getSociety(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(societyService.getSociety(societyId)));
    }

    @GetMapping("/wings")
    public ResponseEntity<ApiResponse<List<WingResponse>>> getWings(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(wingService.getWings(societyId)));
    }

    @PostMapping("/wings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WingResponse>> createWing(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateWingRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        WingResponse response = wingService.createWing(societyId, request, claims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
