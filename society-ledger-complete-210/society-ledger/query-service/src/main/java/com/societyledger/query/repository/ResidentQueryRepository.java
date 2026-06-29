package com.societyledger.query.repository;

import com.societyledger.query.entity.ResidentQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResidentQueryRepository extends JpaRepository<ResidentQuery, Long> {
    Page<ResidentQuery> findBySocietyIdOrderByCreatedAtDesc(Long societyId, Pageable pageable);
    Page<ResidentQuery> findBySocietyIdAndStatusOrderByCreatedAtDesc(
            Long societyId, ResidentQuery.QueryStatus status, Pageable pageable);
    Page<ResidentQuery> findBySocietyIdAndAskedByUserIdOrderByCreatedAtDesc(
            Long societyId, Long userId, Pageable pageable);
    Optional<ResidentQuery> findByIdAndSocietyId(Long id, Long societyId);
}
