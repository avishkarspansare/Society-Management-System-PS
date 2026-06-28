package com.societyledger.finance.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "society-service", url = "${app.society-service.url}")
public interface SocietyServiceClient {

    @GetMapping("/api/v1/internal/societies/{societyId}/flats/count")
    Integer getTotalFlatsCount(@PathVariable Long societyId);
}
