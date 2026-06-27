package com.societyledger.statement.feign;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.society.dto.response.FlatPaymentRefResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "stmt-society-service", url = "${app.society-service.url}")
public interface SocietyServiceClient {

    @GetMapping("/api/v1/societies/{societyId}/flats/payment-refs")
    ResponseEntity<ApiResponse<List<FlatPaymentRefResponse>>> getAllFlatPaymentRefs(@PathVariable Long societyId);
}
