package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateWingRequest;
import com.societyledger.society.dto.response.WingResponse;
import com.societyledger.society.entity.Wing;
import com.societyledger.society.repository.WingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WingService {

    private final WingRepository wingRepository;

    @Transactional
    public WingResponse createWing(Long societyId, CreateWingRequest request, Long adminUserId) {
        if (wingRepository.existsBySocietyIdAndWingName(societyId, request.getWingName())) {
            throw new SocietyLedgerException(
                    "Wing '" + request.getWingName() + "' already exists in this society.",
                    "WING_DUPLICATE", HttpStatus.CONFLICT);
        }
        Wing saved = wingRepository.save(Wing.builder()
                .societyId(societyId)
                .wingName(request.getWingName().trim())
                .totalFloors(request.getTotalFloors())
                .build());
        log.info("Wing created: {} in society {}", saved.getWingName(), societyId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WingResponse> getWings(Long societyId) {
        return wingRepository.findBySocietyIdOrderByWingNameAsc(societyId)
                .stream().map(this::mapToResponse).toList();
    }

    private WingResponse mapToResponse(Wing w) {
        return WingResponse.builder()
                .id(w.getId()).societyId(w.getSocietyId())
                .wingName(w.getWingName()).totalFloors(w.getTotalFloors())
                .flatCount(w.getFlats() != null ? w.getFlats().size() : 0)
                .build();
    }
}
