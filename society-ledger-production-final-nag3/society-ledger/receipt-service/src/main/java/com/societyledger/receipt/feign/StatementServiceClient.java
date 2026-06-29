package com.societyledger.receipt.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign client for statement-service internal API.
 * Used by receipt-service to look up payment records.
 */
@FeignClient(name = "statement-service", url = "${app.statement-service.url}")
public interface StatementServiceClient {

    @GetMapping("/api/v1/internal/statements/{societyId}/payments/{paymentId}")
    PaymentInfo getPaymentInfo(@PathVariable Long societyId,
                               @PathVariable Long paymentId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PaymentInfo {
        private Long paymentId;
        private Long flatId;
        private Long societyId;
        private BigDecimal amount;
        private Integer paymentMonth;
        private Integer paymentYear;
        private String paymentReference;
    }
}
