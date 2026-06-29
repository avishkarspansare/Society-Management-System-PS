package com.societyledger.society.controller;

import com.societyledger.common.dto.ApiResponse;
import com.societyledger.society.dto.response.FlatPaymentRefResponse;
import com.societyledger.society.dto.response.FlatResponse;
import com.societyledger.society.dto.response.SocietyResponse;
import com.societyledger.society.entity.FamilyMember;
import com.societyledger.society.entity.Flat;
import com.societyledger.society.repository.FamilyMemberRepository;
import com.societyledger.society.repository.FlatRepository;
import com.societyledger.society.service.FlatService;
import com.societyledger.society.service.SocietyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal endpoints — consumed only by other microservices via OpenFeign.
 * NOT exposed to the internet. Protected by permitAll in security config
 * because they originate from trusted internal services (no JWT in service-to-service calls).
 *
 * In production, secure with network policies / service mesh (Istio/Linkerd).
 */
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalSocietyController {

    private final FlatService flatService;
    private final SocietyService societyService;
    private final FlatRepository flatRepository;
    private final FamilyMemberRepository familyMemberRepository;

    /**
     * Validate a flat belongs to a society. Called by auth-service on registration.
     */
    @GetMapping("/societies/{societyId}/flats/{flatId}/validate")
    public ResponseEntity<ApiResponse<Void>> validateFlat(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        flatService.validateFlatExists(societyId, flatId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Get flat details including primary resident. Called by receipt-service.
     */
    @GetMapping("/societies/{societyId}/flats/{flatId}")
    public ResponseEntity<ApiResponse<FlatPaymentRefResponse>> getFlatDetails(
            @PathVariable Long societyId, @PathVariable Long flatId) {
        Flat flat = flatRepository.findByIdAndSocietyId(flatId, societyId)
                .orElseThrow(() -> com.societyledger.common.exception.SocietyLedgerException
                        .notFound("Flat", flatId));

        List<FamilyMember> members = familyMemberRepository
                .findByFlatIdAndSocietyId(flatId, societyId);

        String primaryName = members.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                .findFirst()
                .map(FamilyMember::getFullName)
                .orElse(members.isEmpty() ? "Resident" : members.get(0).getFullName());

        FlatPaymentRefResponse response = FlatPaymentRefResponse.builder()
                .flatId(flat.getId())
                .societyId(flat.getSocietyId())
                .flatNumber(flat.getFlatNumber())
                .wingName(flat.getWing() != null ? flat.getWing().getWingName() : "")
                .paymentReferenceCode(flat.getPaymentReferenceCode())
                .primaryResidentName(primaryName)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all flat payment references for a society. Called by statement-service matching engine.
     */
    @GetMapping("/societies/{societyId}/flats/payment-refs")
    public ResponseEntity<ApiResponse<List<FlatPaymentRefResponse>>> getAllFlatPaymentRefs(
            @PathVariable Long societyId) {
        List<Flat> flats = flatRepository.findBySocietyId(societyId);

        List<FlatPaymentRefResponse> refs = flats.stream()
                .map(flat -> {
                    List<FamilyMember> members = familyMemberRepository
                            .findByFlatIdAndSocietyId(flat.getId(), societyId);
                    String primaryName = members.stream()
                            .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                            .findFirst()
                            .map(FamilyMember::getFullName)
                            .orElse("Resident");
                    return FlatPaymentRefResponse.builder()
                            .flatId(flat.getId())
                            .societyId(societyId)
                            .flatNumber(flat.getFlatNumber())
                            .wingName(flat.getWing() != null ? flat.getWing().getWingName() : "")
                            .paymentReferenceCode(flat.getPaymentReferenceCode())
                            .primaryResidentName(primaryName)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(refs));
    }

    /**
     * Get total flat count. Called by finance-service for monthly summary.
     */
    @GetMapping("/societies/{societyId}/flats/count")
    public ResponseEntity<ApiResponse<Integer>> getTotalFlatsCount(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(flatRepository.countBySocietyId(societyId)));
    }

    /**
     * Get society details. Called by receipt-service for PDF generation.
     */
    @GetMapping("/societies/{societyId}")
    public ResponseEntity<ApiResponse<SocietyResponse>> getSociety(
            @PathVariable Long societyId) {
        return ResponseEntity.ok(ApiResponse.success(societyService.getSocietyInternal(societyId)));
    }
}
