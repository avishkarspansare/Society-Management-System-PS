package com.societyledger.audit.controller;

import com.societyledger.audit.dto.response.AuditReportResponse;
import com.societyledger.audit.service.AuditReportService;
import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit/{societyId}/reports")
@RequiredArgsConstructor
public class AuditController {

    private final AuditReportService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditReportResponse>> upload(
            @PathVariable Long societyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "periodFrom", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(value = "periodTo", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                service.uploadAuditReport(societyId, title, description, periodFrom, periodTo,
                        file, claims.getUserId()),
                "Audit report uploaded."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditReportResponse>>> getAll(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getReports(societyId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditReportResponse>> publish(
            @PathVariable Long societyId, @PathVariable Long id,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(service.publish(societyId, id),
                "Audit report published."));
    }
}
