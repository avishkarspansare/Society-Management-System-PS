package com.societyledger.audit.controller;

import com.societyledger.audit.dto.request.UploadAuditReportRequest;
import com.societyledger.audit.dto.response.AuditReportResponse;
import com.societyledger.audit.service.AuditService;
import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/audit/{societyId}/reports")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditReportResponse>>> getReports(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                auditService.getReports(societyId,
                        PageRequest.of(page, size, Sort.by("auditYear").descending()))));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AuditReportResponse>> getById(
            @PathVariable Long societyId, @PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getReportById(societyId, reportId)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditReportResponse>> upload(
            @PathVariable Long societyId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") @Valid UploadAuditReportRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        AuditReportResponse response = auditService.uploadReport(
                societyId, file, request, claims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Audit report uploaded."));
    }

    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long societyId, @PathVariable Long reportId) {
        byte[] bytes = auditService.downloadReport(societyId, reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"audit-report-" + reportId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
