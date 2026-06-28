package com.societyledger.receipt.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "society-service", url = "${app.society-service.url}")
public interface SocietyServiceClient {

    @GetMapping("/api/v1/internal/societies/{societyId}/flats/{flatId}")
    FlatDetails getFlatDetails(@PathVariable Long societyId, @PathVariable Long flatId);

    @GetMapping("/api/v1/internal/societies/{societyId}")
    SocietyDetails getSocietyDetails(@PathVariable Long societyId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FlatDetails {
        private Long flatId;
        private String flatNumber;
        private String wingName;
        private String primaryResidentName;
        private String paymentReferenceCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SocietyDetails {
        private Long id;
        private String societyName;
        private String city;
    }
}
