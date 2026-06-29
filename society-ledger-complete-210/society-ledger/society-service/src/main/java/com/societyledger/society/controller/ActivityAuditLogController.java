package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.society.entity.ActivityAuditLog;
import com.societyledger.society.repository.ActivityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/societies/{societyId}/activity-log")
@RequiredArgsConstructor
public class ActivityAuditLogController {

    private final ActivityAuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ActivityAuditLog>>> getLog(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(repository.findBySocietyIdOrderByCreatedAtDesc(
                        societyId, PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }
}
