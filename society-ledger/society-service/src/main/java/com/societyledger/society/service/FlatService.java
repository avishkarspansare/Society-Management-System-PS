package com.societyledger.society.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateFlatRequest;
import com.societyledger.society.dto.response.FlatPaymentRefResponse;
import com.societyledger.society.dto.response.FlatResponse;
import com.societyledger.society.entity.Flat;
import com.societyledger.society.entity.Wing;
import com.societyledger.society.repository.FlatRepository;
import com.societyledger.society.repository.WingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;
    private final WingRepository wingRepository;
    private final ActivityAuditLogService auditLogService;

    @Transactional
    public FlatResponse createFlat(Long societyId, CreateFlatRequest request, Long adminUserId) {
        Wing wing = wingRepository.findByIdAndSocietyId(request.getWingId(), societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Wing", request.getWingId()));

        if (flatRepository.existsBySocietyIdAndWingIdAndFlatNumber(
                societyId, request.getWingId(), request.getFlatNumber())) {
            throw new SocietyLedgerException(
                    "Flat " + request.getFlatNumber() + " already exists in this wing.",
                    "FLAT_DUPLICATE", HttpStatus.CONFLICT);
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
        return mapToFlatResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<FlatResponse> getFlats(Long societyId, Pageable pageable) {
        return PageResponse.from(flatRepository.findBySocietyId(societyId, pageable)
                .map(this::mapToFlatResponse));
    }

    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long societyId, Long flatId) {
        return flatRepository.findByIdAndSocietyId(flatId, societyId)
                .map(this::mapToFlatResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("Flat", flatId));
    }

    @Transactional(readOnly = true)
    public void validateFlatExists(Long societyId, Long flatId) {
        if (!flatRepository.existsByIdAndSocietyId(flatId, societyId)) {
            throw SocietyLedgerException.notFound("Flat", flatId);
        }
    }

    @Transactional(readOnly = true)
    public List<FlatPaymentRefResponse> getAllFlatPaymentRefs(Long societyId) {
        return flatRepository.findAllBySocietyIdWithFamilyMembers(societyId)
                .stream()
                .map(this::mapToPaymentRef)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlatPaymentRefResponse getFlatPaymentRefById(Long societyId, Long flatId) {
        Flat flat = flatRepository.findByIdAndSocietyId(flatId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Flat", flatId));
        return mapToPaymentRef(flat);
    }

    private String generatePaymentReference(String wingName, String flatNumber) {
        String wingPrefix = wingName.replaceAll("(?i)wing\\s*", "").trim().toUpperCase();
        return wingPrefix + flatNumber.toUpperCase() + "-MNT";
    }

    private String getPrimaryResidentName(Flat flat) {
        if (flat.getFamilyMembers() == null || flat.getFamilyMembers().isEmpty()) return null;
        return flat.getFamilyMembers().stream()
                .filter(fm -> Boolean.TRUE.equals(fm.getIsPrimary()))
                .map(fm -> fm.getFirstName() + " " + fm.getLastName())
                .findFirst()
                .orElse(flat.getFamilyMembers().get(0).getFirstName() + " "
                        + flat.getFamilyMembers().get(0).getLastName());
    }

    private String getPrimaryResidentEmail(Flat flat) {
        if (flat.getFamilyMembers() == null || flat.getFamilyMembers().isEmpty()) return null;
        return flat.getFamilyMembers().stream()
                .filter(fm -> Boolean.TRUE.equals(fm.getIsPrimary()))
                .map(fm -> fm.getEmail())
                .findFirst().orElse(null);
    }

    private FlatResponse mapToFlatResponse(Flat f) {
        return FlatResponse.builder()
                .id(f.getId()).societyId(f.getSocietyId())
                .wingId(f.getWing().getId()).wingName(f.getWing().getWingName())
                .flatNumber(f.getFlatNumber()).floorNumber(f.getFloorNumber())
                .areaSqft(f.getAreaSqft()).paymentReferenceCode(f.getPaymentReferenceCode())
                .isOccupied(f.getIsOccupied())
                .primaryResidentName(getPrimaryResidentName(f))
                .createdAt(f.getCreatedAt())
                .build();
    }

    private FlatPaymentRefResponse mapToPaymentRef(Flat f) {
        return FlatPaymentRefResponse.builder()
                .flatId(f.getId()).societyId(f.getSocietyId())
                .flatNumber(f.getFlatNumber())
                .wingName(f.getWing() != null ? f.getWing().getWingName() : "")
                .paymentReferenceCode(f.getPaymentReferenceCode())
                .primaryResidentName(getPrimaryResidentName(f))
                .primaryResidentEmail(getPrimaryResidentEmail(f))
                .build();
    }
}
