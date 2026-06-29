package com.societyledger.query.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.query.dto.request.AnswerQueryRequest;
import com.societyledger.query.dto.request.CreateQueryRequest;
import com.societyledger.query.dto.response.ResidentQueryResponse;
import com.societyledger.query.service.QueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queries/{societyId}")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentQueryResponse>> createQuery(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateQueryRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.createQuery(societyId, request, claims),
                "Query submitted successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ResidentQueryResponse>>> getQueries(
            @PathVariable Long societyId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.getQueries(societyId, status, claims.getRole(), claims.getUserId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PatchMapping("/{queryId}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResidentQueryResponse>> answerQuery(
            @PathVariable Long societyId, @PathVariable Long queryId,
            @Valid @RequestBody AnswerQueryRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.answerQuery(societyId, queryId, request, claims.getUserId()),
                "Query answered."));
    }

    @PatchMapping("/{queryId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResidentQueryResponse>> closeQuery(
            @PathVariable Long societyId, @PathVariable Long queryId,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.closeQuery(societyId, queryId, claims.getUserId()),
                "Query closed."));
    }
}
