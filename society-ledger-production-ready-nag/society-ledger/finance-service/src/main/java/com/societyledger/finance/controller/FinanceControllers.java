package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.finance.dto.request.CreateAnnouncementRequest;
import com.societyledger.finance.dto.request.CreateCategoryRequest;
import com.societyledger.finance.dto.response.*;
import com.societyledger.finance.entity.Announcement;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.entity.TransparencyTimeline;
import com.societyledger.finance.repository.AnnouncementRepository;
import com.societyledger.finance.repository.ExpenseCategoryRepository;
import com.societyledger.finance.repository.TransparencyTimelineRepository;
import com.societyledger.finance.service.MonthlySummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// ─── Expense Categories ──────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/expense-categories")
@RequiredArgsConstructor
class ExpenseCategoryController {
    private final ExpenseCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategoryResponse>>> getCategories(
            @PathVariable Long societyId) {
        List<ExpenseCategoryResponse> categories = categoryRepository.findBySocietyId(societyId)
                .stream()
                .map(c -> ExpenseCategoryResponse.builder()
                        .id(c.getId()).societyId(c.getSocietyId())
                        .name(c.getName()).description(c.getDescription())
                        .createdAt(c.getCreatedAt()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseCategoryResponse>> create(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateCategoryRequest request) {
        if (categoryRepository.existsBySocietyIdAndNameIgnoreCase(societyId, request.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Category '" + request.getName() + "' already exists.", "DUPLICATE"));
        }
        ExpenseCategory saved = categoryRepository.save(ExpenseCategory.builder()
                .societyId(societyId).name(request.getName().trim())
                .description(request.getDescription()).build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ExpenseCategoryResponse.builder()
                        .id(saved.getId()).societyId(saved.getSocietyId())
                        .name(saved.getName()).description(saved.getDescription())
                        .createdAt(saved.getCreatedAt()).build()));
    }
}

// ─── Financial Dashboard ──────────────────────────────────────────
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

// ─── Transparency Timeline ──────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/timeline")
@RequiredArgsConstructor
class TimelineController {
    private final TransparencyTimelineRepository timelineRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TimelineResponse>>> getTimeline(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TimelineResponse> result = PageResponse.from(
                timelineRepository.findBySocietyIdOrderByOccurredAtDesc(societyId,
                        PageRequest.of(page, size))
                        .map(t -> TimelineResponse.builder()
                                .id(t.getId()).societyId(t.getSocietyId())
                                .eventType(t.getEventType()).eventSummary(t.getEventSummary())
                                .referenceId(t.getReferenceId()).referenceType(t.getReferenceType())
                                .occurredAt(t.getOccurredAt()).build()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

// ─── Announcements ──────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/finance/{societyId}/announcements")
@RequiredArgsConstructor
class AnnouncementController {
    private final AnnouncementRepository announcementRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AnnouncementResponse>>> getAnnouncements(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AnnouncementResponse> result = PageResponse.from(
                announcementRepository.findBySocietyIdAndIsActiveTrueOrderByCreatedAtDesc(
                        societyId, PageRequest.of(page, size))
                        .map(a -> AnnouncementResponse.builder()
                                .id(a.getId()).societyId(a.getSocietyId())
                                .title(a.getTitle()).body(a.getBody())
                                .category(a.getCategory()).isActive(a.getIsActive())
                                .createdAt(a.getCreatedAt()).expiresAt(a.getExpiresAt()).build()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @PathVariable Long societyId,
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal JwtClaims claims) {
        Announcement saved = announcementRepository.save(Announcement.builder()
                .societyId(societyId).title(request.getTitle())
                .body(request.getBody()).category(request.getCategory())
                .createdBy(claims.getUserId()).expiresAt(request.getExpiresAt()).build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AnnouncementResponse.builder()
                        .id(saved.getId()).title(saved.getTitle())
                        .body(saved.getBody()).category(saved.getCategory())
                        .isActive(saved.getIsActive()).createdAt(saved.getCreatedAt()).build()));
    }

    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long societyId, @PathVariable Long announcementId) {
        announcementRepository.findByIdAndSocietyId(announcementId, societyId)
                .ifPresent(a -> { a.setIsActive(false); announcementRepository.save(a); });
        return ResponseEntity.ok(ApiResponse.success(null, "Announcement removed."));
    }
}
