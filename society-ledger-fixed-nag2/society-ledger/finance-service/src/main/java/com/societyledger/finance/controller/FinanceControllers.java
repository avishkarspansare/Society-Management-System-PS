package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.finance.dto.request.CreateAnnouncementRequest;
import com.societyledger.finance.dto.request.CreateCategoryRequest;
import com.societyledger.finance.dto.response.*;
import com.societyledger.finance.service.AnnouncementService;
import com.societyledger.finance.service.ExpenseCategoryService;
import com.societyledger.finance.service.MonthlySummaryService;
import com.societyledger.finance.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ─── Expense Categories ──────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/expense-categories")
@RequiredArgsConstructor
class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategoryResponse>>> getCategories(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategories(societyId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseCategoryResponse>> create(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(categoryService.create(societyId, request)));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long societyId,
            @PathVariable Long categoryId) {
        categoryService.delete(societyId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

// ─── Financial Dashboard ─────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}")
@RequiredArgsConstructor
class FinanceDashboardController {

    private final MonthlySummaryService monthlySummaryService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<FinancialDashboardResponse>> getDashboard(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(
                monthlySummaryService.getDashboard(societyId)));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getMonthlySummary(
            @PathVariable Long societyId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(
                monthlySummaryService.getSummaries(societyId, year)));
    }
}

// ─── Transparency Timeline ────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/timeline")
@RequiredArgsConstructor
class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TimelineResponse>>> getTimeline(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getTimeline(societyId, page, size)));
    }
}

// ─── Announcements ────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/announcements")
@RequiredArgsConstructor
class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAnnouncements(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                announcementService.getActiveAnnouncements(societyId, page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        announcementService.create(societyId, request, claims.getUserId())));
    }

    @PatchMapping("/{announcementId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long societyId,
            @PathVariable Long announcementId) {
        announcementService.deactivate(societyId, announcementId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
