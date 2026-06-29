package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.dto.request.CreateFamilyMemberRequest;
import com.societyledger.society.dto.response.FamilyMemberResponse;
import com.societyledger.society.entity.FamilyMember;
import com.societyledger.society.entity.Flat;
import com.societyledger.society.repository.FamilyMemberRepository;
import com.societyledger.society.repository.FlatRepository;
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
public class FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final FlatRepository flatRepository;

    @Transactional
    public FamilyMemberResponse addFamilyMember(Long societyId, Long flatId,
                                                  CreateFamilyMemberRequest request,
                                                  Long requestingUserId) {
        Flat flat = flatRepository.findByIdAndSocietyId(flatId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Flat", flatId));

        // Enforce: only one primary member per flat
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            List<FamilyMember> existing = familyMemberRepository
                    .findByFlatIdAndSocietyId(flatId, societyId);
            existing.forEach(m -> {
                if (Boolean.TRUE.equals(m.getIsPrimary())) {
                    m.setIsPrimary(false);
                    familyMemberRepository.save(m);
                }
            });
        }

        FamilyMember member = FamilyMember.builder()
                .flat(flat)
                .societyId(societyId)
                .fullName(request.getFullName().trim())
                .relation(request.getRelation())
                .dateOfBirth(request.getDateOfBirth())
                .phone(request.getPhone())
                .isPrimary(Boolean.TRUE.equals(request.getIsPrimary()))
                .build();

        return mapToResponse(familyMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getFamilyMembers(Long societyId, Long flatId) {
        return familyMemberRepository.findByFlatIdAndSocietyId(flatId, societyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFamilyMember(Long societyId, Long flatId, Long memberId) {
        if (!flatRepository.existsByIdAndSocietyId(flatId, societyId)) {
            throw SocietyLedgerException.notFound("Flat", flatId);
        }
        familyMemberRepository.deleteByIdAndFlatIdAndSocietyId(memberId, flatId, societyId);
    }

    private FamilyMemberResponse mapToResponse(FamilyMember m) {
        return FamilyMemberResponse.builder()
                .id(m.getId())
                .flatId(m.getFlat().getId())
                .fullName(m.getFullName())
                .relation(m.getRelation().name())
                .dateOfBirth(m.getDateOfBirth())
                .phone(m.getPhone())
                .isPrimary(m.getIsPrimary())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
