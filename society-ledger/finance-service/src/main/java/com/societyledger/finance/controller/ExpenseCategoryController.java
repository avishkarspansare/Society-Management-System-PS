package com.societyledger.finance.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.finance.entity.ExpenseCategory;
import com.societyledger.finance.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/{societyId}/expense-categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseCategory>> create(
            @PathVariable Long societyId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.create(societyId, body.get("name"), body.get("description")),
                "Category created."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getAll(@PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll(societyId)));
    }
}
