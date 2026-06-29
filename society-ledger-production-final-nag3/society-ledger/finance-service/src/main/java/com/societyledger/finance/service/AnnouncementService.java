package com.societyledger.finance.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.dto.request.CreateAnnouncementRequest;
import com.societyledger.finance.dto.response.AnnouncementResponse;
import com.societyledger.finance.entity.Announcement;
import com.societyledger.finance.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getActiveAnnouncements(Long societyId, int page, int size) {
        return announcementRepository
                .findBySocietyIdAndIsActiveTrueOrderByCreatedAtDesc(societyId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementResponse create(Long societyId, CreateAnnouncementRequest request,
                                       Long adminUserId) {
        Announcement announcement = Announcement.builder()
                .societyId(societyId)
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .category(request.getCategory())
                .isActive(true)
                .createdBy(adminUserId)
                .expiresAt(request.getExpiresAt())
                .build();
        Announcement saved = announcementRepository.save(announcement);
        log.info("Announcement created for society {}: {}", societyId, saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional
    public void deactivate(Long societyId, Long announcementId) {
        Announcement a = announcementRepository.findById(announcementId)
                .filter(ann -> ann.getSocietyId().equals(societyId))
                .orElseThrow(() -> SocietyLedgerException.notFound("Announcement", announcementId));
        a.setIsActive(false);
        announcementRepository.save(a);
        log.info("Announcement {} deactivated for society {}", announcementId, societyId);
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .societyId(a.getSocietyId())
                .title(a.getTitle())
                .body(a.getBody())
                .category(a.getCategory())
                .isActive(a.getIsActive())
                .createdBy(a.getCreatedBy())
                .createdAt(a.getCreatedAt())
                .expiresAt(a.getExpiresAt())
                .build();
    }
}
