package com.societyledger.finance.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "statement-service", url = "${app.statement-service.url}")
public interface StatementServiceClient {

    @GetMapping("/api/v1/internal/statements/{societyId}/income")
    BigDecimal getTotalIncomeForMonth(
            @PathVariable Long societyId,
            @RequestParam int year,
            @RequestParam int month);

    @GetMapping("/api/v1/internal/statements/{societyId}/paid-flats-count")
    Integer getPaidFlatsCountForMonth(
            @PathVariable Long societyId,
            @RequestParam int year,
            @RequestParam int month);
}
