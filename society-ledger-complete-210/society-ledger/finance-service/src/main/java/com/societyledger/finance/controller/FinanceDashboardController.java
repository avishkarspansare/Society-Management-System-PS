package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.finance.dto.response.FinancialDashboardResponse;
import com.societyledger.finance.dto.response.MonthlySummaryResponse;
import com.societyledger.finance.entity.FinancialTimeline;
import com.societyledger.finance.service.MonthlySummaryService;
import com.societyledger.finance.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/{societyId}")
@RequiredArgsConstructor
public class FinanceDashboardController {

    private final MonthlySummaryService summaryService;
    private final TimelineService timelineService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<FinancialDashboardResponse>> getDashboard(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(summaryService.getDashboard(societyId)));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getSummary(
            @PathVariable Long societyId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(summaryService.getSummaries(societyId, year)));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<?>> getTimeline(
            @PathVariable Long societyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                timelineService.getTimeline(societyId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    /** Internal endpoint for statement-service to report income totals. */
    @GetMapping("/income")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotalIncome(
            @PathVariable Long societyId,
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(java.math.BigDecimal.ZERO)); // placeholder; driven by statement-service
    }
}
