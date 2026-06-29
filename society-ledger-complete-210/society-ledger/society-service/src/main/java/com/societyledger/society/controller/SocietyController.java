package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateSocietyRequest;
import com.societyledger.society.dto.response.SocietyResponse;
import com.societyledger.society.service.SocietyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/societies")
@RequiredArgsConstructor
public class SocietyController {

    private final SocietyService societyService;

    /** Super-admin endpoint: create a new society (onboarding). */
    @PostMapping
    public ResponseEntity<ApiResponse<SocietyResponse>> createSociety(
            @Valid @RequestBody CreateSocietyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(societyService.createSociety(request),
                "Society created successfully."));
    }

    @GetMapping("/{societyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<SocietyResponse>> getSociety(@PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(societyService.getSocietyById(societyId)));
    }
}
