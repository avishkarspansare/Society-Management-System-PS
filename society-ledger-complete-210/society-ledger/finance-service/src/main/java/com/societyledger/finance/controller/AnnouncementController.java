package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.finance.entity.Announcement;
import com.societyledger.finance.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/{societyId}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> create(
            @PathVariable Long societyId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                announcementService.create(societyId, (String) body.get("title"),
                        (String) body.get("content"),
                        body.containsKey("isPinned") ? (Boolean) body.get("isPinned") : false,
                        claims.getUserId()),
                "Announcement posted."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Announcement>>> getAll(@PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getAll(societyId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long societyId, @PathVariable Long id,
            @AuthenticationPrincipal JwtClaims claims) {
        announcementService.delete(societyId, id, claims.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Announcement deleted."));
    }
}
