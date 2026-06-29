package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.society.dto.request.CreateFamilyMemberRequest;
import com.societyledger.society.dto.response.FamilyMemberResponse;
import com.societyledger.society.service.FamilyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/societies/{societyId}/flats/{flatId}/family-members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FamilyMemberResponse>>> getMembers(
            @PathVariable Long societyId, @PathVariable Long flatId,
            @AuthenticationPrincipal JwtClaims claims) {
        // Residents can only see their own flat's members
        if ("RESIDENT".equals(claims.getRole()) && !claims.getFlatId().equals(flatId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied.", "FORBIDDEN"));
        }
        return ResponseEntity.ok(ApiResponse.success(
                familyMemberService.getFamilyMembers(societyId, flatId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> addMember(
            @PathVariable Long societyId, @PathVariable Long flatId,
            @Valid @RequestBody CreateFamilyMemberRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        // Residents can only add to their own flat
        if ("RESIDENT".equals(claims.getRole()) && !claims.getFlatId().equals(flatId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied.", "FORBIDDEN"));
        }
        FamilyMemberResponse response =
                familyMemberService.addFamilyMember(societyId, flatId, request, claims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @PathVariable Long societyId, @PathVariable Long flatId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal JwtClaims claims) {
        familyMemberService.deleteFamilyMember(societyId, flatId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null, "Family member removed."));
    }
}
