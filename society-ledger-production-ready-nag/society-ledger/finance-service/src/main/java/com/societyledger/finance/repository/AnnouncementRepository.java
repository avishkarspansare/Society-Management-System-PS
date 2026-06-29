package com.societyledger.finance.repository;
import com.societyledger.finance.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Page<Announcement> findBySocietyIdAndIsActiveTrueOrderByCreatedAtDesc(Long societyId, Pageable p);
    Optional<Announcement> findByIdAndSocietyId(Long id, Long societyId);
}
