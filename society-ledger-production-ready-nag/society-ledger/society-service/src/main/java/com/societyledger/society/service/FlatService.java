package com.societyledger.society.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateFlatRequest;
import com.societyledger.society.dto.response.FlatResponse;
import com.societyledger.society.entity.Flat;
import com.societyledger.society.entity.Wing;
import com.societyledger.society.mapper.FlatMapper;
import com.societyledger.society.repository.FlatRepository;
import com.societyledger.society.repository.WingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;
    private final WingRepository wingRepository;
    private final FlatMapper flatMapper;
    private final ActivityAuditLogService auditLogService;

    @Transactional
    public FlatResponse createFlat(Long societyId, CreateFlatRequest request, Long adminUserId) {
        Wing wing = wingRepository.findByIdAndSocietyId(request.getWingId(), societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Wing", request.getWingId()));

        if (flatRepository.existsBySocietyIdAndWingIdAndFlatNumber(
                societyId, request.getWingId(), request.getFlatNumber())) {
            throw new SocietyLedgerException(
                    "Flat " + request.getFlatNumber() + " already exists in this wing.",
                    "FLAT_DUPLICATE",
                    HttpStatus.CONFLICT
            );
        }

        String paymentRef = generatePaymentReference(wing.getWingName(), request.getFlatNumber());

        Flat flat = Flat.builder()
                .societyId(societyId)
                .wing(wing)
                .flatNumber(request.getFlatNumber().toUpperCase())
                .floorNumber(request.getFloorNumber())
                .areaSqft(request.getAreaSqft())
                .paymentReferenceCode(paymentRef)
                .isOccupied(request.getIsOccupied() != null ? request.getIsOccupied() : true)
                .build();

        Flat saved = flatRepository.save(flat);
        auditLogService.log(societyId, adminUserId, "FLAT_CREATED", "Flat", saved.getId(),
                "Flat " + saved.getFlatNumber() + " created in wing " + wing.getWingName());

        log.info("Flat created: {} in society {}", saved.getFlatNumber(), societyId);
        return flatMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<FlatResponse> getFlats(Long societyId, Pageable pageable) {
        return PageResponse.from(
                flatRepository.findBySocietyId(societyId, pageable)
                        .map(flatMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long societyId, Long flatId) {
        return flatRepository.findByIdAndSocietyId(flatId, societyId)
                .map(flatMapper::toResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("Flat", flatId));
    }

    // Called internally by other services (e.g. auth-service via Feign)
    @Transactional(readOnly = true)
    public void validateFlatExists(Long societyId, Long flatId) {
        if (!flatRepository.existsByIdAndSocietyId(flatId, societyId)) {
            throw SocietyLedgerException.notFound("Flat", flatId);
        }
    }

    @Transactional(readOnly = true)
    public String getPaymentReference(Long societyId, Long flatId) {
        return flatRepository.findByIdAndSocietyId(flatId, societyId)
                .map(Flat::getPaymentReferenceCode)
                .orElseThrow(() -> SocietyLedgerException.notFound("Flat", flatId));
    }

    /**
     * Generates a unique payment reference like "A403-MNT" from wing name and flat number.
     */
    private String generatePaymentReference(String wingName, String flatNumber) {
        // Normalize wing prefix: "Wing A" -> "A", "A" -> "A"
        String wingPrefix = wingName.replaceAll("(?i)wing\\s*", "").trim().toUpperCase();
        return wingPrefix + flatNumber.toUpperCase() + "-MNT";
    }
}
