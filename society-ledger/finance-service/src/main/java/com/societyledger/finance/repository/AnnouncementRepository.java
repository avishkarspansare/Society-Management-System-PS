package com.societyledger.finance.repository;

import com.societyledger.finance.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findBySocietyIdOrderByIsPinnedDescCreatedAtDesc(Long societyId);
    Optional<Announcement> findByIdAndSocietyId(Long id, Long societyId);
}
