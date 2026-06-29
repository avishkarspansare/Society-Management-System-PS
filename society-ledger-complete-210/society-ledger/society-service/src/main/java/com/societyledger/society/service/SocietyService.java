package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateSocietyRequest;
import com.societyledger.society.dto.response.SocietyResponse;
import com.societyledger.society.entity.Society;
import com.societyledger.society.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocietyService {

    private final SocietyRepository societyRepository;

    @Transactional
    public SocietyResponse createSociety(CreateSocietyRequest request) {
        if (request.getRegistrationNumber() != null &&
            societyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new SocietyLedgerException(
                    "Society with this registration number already exists.",
                    "SOCIETY_EXISTS", HttpStatus.CONFLICT);
        }
        Society saved = societyRepository.save(Society.builder()
                .societyName(request.getSocietyName().strip())
                .registrationNumber(request.getRegistrationNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .build());
        log.info("Society created: {} ({})", saved.getSocietyName(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SocietyResponse getSocietyById(Long societyId) {
        return societyRepository.findById(societyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("Society", societyId));
    }

    private SocietyResponse mapToResponse(Society s) {
        return SocietyResponse.builder()
                .id(s.getId())
                .societyName(s.getSocietyName())
                .registrationNumber(s.getRegistrationNumber())
                .address(s.getAddress())
                .city(s.getCity())
                .state(s.getState())
                .pinCode(s.getPinCode())
                .contactEmail(s.getContactEmail())
                .contactPhone(s.getContactPhone())
                .subscriptionPlan(s.getSubscriptionPlan())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
