package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateWingRequest;
import com.societyledger.society.dto.response.WingResponse;
import com.societyledger.society.entity.Society;
import com.societyledger.society.entity.Wing;
import com.societyledger.society.repository.SocietyRepository;
import com.societyledger.society.repository.WingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WingService {

    private final WingRepository wingRepository;
    private final SocietyRepository societyRepository;
    private final ActivityAuditLogService auditLogService;

    @Transactional
    public WingResponse createWing(Long societyId, CreateWingRequest request, Long adminUserId) {
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Society", societyId));

        if (wingRepository.existsBySocietyIdAndWingNameIgnoreCase(societyId, request.getWingName())) {
            throw new SocietyLedgerException(
                    "Wing '" + request.getWingName() + "' already exists in this society.",
                    "WING_DUPLICATE", HttpStatus.CONFLICT);
        }

        Wing wing = Wing.builder()
                .society(society)
                .wingName(request.getWingName().trim().toUpperCase())
                .build();

        Wing saved = wingRepository.save(wing);
        auditLogService.log(societyId, adminUserId, "WING_CREATED", "Wing", saved.getId(),
                "Wing " + saved.getWingName() + " created");
        log.info("Wing created: {} in society {}", saved.getWingName(), societyId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WingResponse> getWings(Long societyId) {
        return wingRepository.findBySocietyId(societyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WingResponse mapToResponse(Wing w) {
        return WingResponse.builder()
                .id(w.getId())
                .societyId(w.getSociety().getId())
                .wingName(w.getWingName())
                .flatCount(w.getFlats() != null ? w.getFlats().size() : 0)
                .createdAt(w.getCreatedAt())
                .build();
    }
}
