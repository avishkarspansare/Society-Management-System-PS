package com.societyledger.finance.repository;
import com.societyledger.finance.entity.TransparencyTimeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TransparencyTimelineRepository extends JpaRepository<TransparencyTimeline, Long> {
    Page<TransparencyTimeline> findBySocietyIdOrderByOccurredAtDesc(Long societyId, Pageable p);
}
