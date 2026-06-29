package com.societyledger.finance.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.finance.entity.Announcement;
import com.societyledger.finance.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository repository;

    @Transactional
    public Announcement create(Long societyId, String title, String content,
                               Boolean isPinned, Long adminUserId) {
        return repository.save(Announcement.builder()
                .societyId(societyId).title(title.strip()).content(content.strip())
                .isPinned(isPinned != null && isPinned).createdBy(adminUserId)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAll(Long societyId) {
        return repository.findBySocietyIdOrderByIsPinnedDescCreatedAtDesc(societyId);
    }

    @Transactional
    public void delete(Long societyId, Long id, Long adminUserId) {
        Announcement ann = repository.findByIdAndSocietyId(id, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Announcement", id));
        repository.delete(ann);
    }
}
