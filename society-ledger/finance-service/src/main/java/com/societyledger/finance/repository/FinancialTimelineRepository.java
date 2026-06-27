package com.societyledger.finance.repository;

import com.societyledger.finance.entity.FinancialTimeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialTimelineRepository extends JpaRepository<FinancialTimeline, Long> {
    Page<FinancialTimeline> findBySocietyIdOrderByCreatedAtDesc(Long societyId, Pageable pageable);
}
