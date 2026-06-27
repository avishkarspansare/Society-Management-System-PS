package com.societyledger.receipt.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "receipt-society-service", url = "${app.society-service.url}")
public interface SocietyServiceClient {

    @GetMapping("/api/v1/societies/{societyId}/flats/{flatId}/payment-ref")
    ResponseEntity<Object> getFlatPaymentRef(@PathVariable Long societyId, @PathVariable Long flatId);
}
