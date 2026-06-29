package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.response.SocietyResponse;
import com.societyledger.society.entity.Society;
import com.societyledger.society.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocietyService {

    private final SocietyRepository societyRepository;

    @Transactional(readOnly = true)
    public SocietyResponse getSociety(Long societyId) {
        Society society = societyRepository.findByIdAndIsActiveTrue(societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Society", societyId));
        return mapToResponse(society);
    }

    @Transactional(readOnly = true)
    public SocietyResponse getSocietyInternal(Long societyId) {
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Society", societyId));
        return mapToResponse(society);
    }

    private SocietyResponse mapToResponse(Society s) {
        return SocietyResponse.builder()
                .id(s.getId())
                .societyName(s.getSocietyName())
                .registrationNumber(s.getRegistrationNumber())
                .address(s.getAddress())
                .city(s.getCity())
                .state(s.getState())
                .pincode(s.getPincode())
                .contactEmail(s.getContactEmail())
                .contactPhone(s.getContactPhone())
                .planName(s.getPlan() != null ? s.getPlan().getPlanName() : null)
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
