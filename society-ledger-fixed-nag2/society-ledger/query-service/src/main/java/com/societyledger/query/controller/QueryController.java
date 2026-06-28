package com.societyledger.query.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.query.dto.request.CreateQueryRequest;
import com.societyledger.query.dto.request.RespondToQueryRequest;
import com.societyledger.query.dto.response.PublicQueryResponse;
import com.societyledger.query.service.QueryService;
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
@RequestMapping("/api/v1/queries/{societyId}")
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PublicQueryResponse>>> getQueries(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.getQueries(societyId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<ApiResponse<PublicQueryResponse>> getById(
            @PathVariable Long societyId, @PathVariable Long queryId) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.getQueryById(societyId, queryId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PublicQueryResponse>> createQuery(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateQueryRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        PublicQueryResponse response = queryService.createQuery(
                societyId, claims.getFlatId(), claims.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Query submitted. Admin will respond soon."));
    }

    @PostMapping("/{queryId}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PublicQueryResponse>> respond(
            @PathVariable Long societyId,
            @PathVariable Long queryId,
            @Valid @RequestBody RespondToQueryRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.respondToQuery(societyId, queryId, claims.getUserId(), request),
                "Response posted."));
    }

    @PatchMapping("/{queryId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PublicQueryResponse>> close(
            @PathVariable Long societyId, @PathVariable Long queryId) {
        return ResponseEntity.ok(ApiResponse.success(
                queryService.closeQuery(societyId, queryId), "Query closed."));
    }
}
