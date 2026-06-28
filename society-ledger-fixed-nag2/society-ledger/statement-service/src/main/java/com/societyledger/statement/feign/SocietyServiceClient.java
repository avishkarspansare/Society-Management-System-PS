package com.societyledger.statement.feign;

import com.societyledger.statement.dto.FlatPaymentRefDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "society-service", url = "${app.society-service.url}")
public interface SocietyServiceClient {

    @GetMapping("/api/v1/internal/societies/{societyId}/flats/payment-refs")
    List<FlatPaymentRefDto> getAllFlatPaymentRefs(@PathVariable Long societyId);

    @GetMapping("/api/v1/internal/societies/{societyId}/flats/{flatId}")
    FlatPaymentRefDto getFlatPaymentRef(@PathVariable Long societyId, @PathVariable Long flatId);
}
