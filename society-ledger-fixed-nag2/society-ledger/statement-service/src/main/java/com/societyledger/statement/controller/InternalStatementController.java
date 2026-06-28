package com.societyledger.statement.controller;

import com.societyledger.statement.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Internal endpoints consumed by finance-service via Feign.
 */
@RestController
@RequestMapping("/api/v1/internal/statements")
@RequiredArgsConstructor
public class InternalStatementController {

    private final PaymentRecordRepository paymentRecordRepository;

    @GetMapping("/{societyId}/income")
    public BigDecimal getTotalIncomeForMonth(
            @PathVariable Long societyId,
            @RequestParam int year,
            @RequestParam int month) {
        return paymentRecordRepository.sumIncomeForMonth(societyId, year, month);
    }

    @GetMapping("/{societyId}/paid-flats-count")
    public Integer getPaidFlatsCountForMonth(
            @PathVariable Long societyId,
            @RequestParam int year,
            @RequestParam int month) {
        return paymentRecordRepository.countPaidFlats(societyId, year, month);
    }
}
