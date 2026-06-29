package com.societyledger.query.repository;

import com.societyledger.query.entity.PublicQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PublicQueryRepository extends JpaRepository<PublicQuery, Long> {
    Page<PublicQuery> findBySocietyIdOrderByCreatedAtDesc(Long societyId, Pageable p);
    Page<PublicQuery> findBySocietyIdAndStatusOrderByCreatedAtDesc(
            Long societyId, PublicQuery.QueryStatus status, Pageable p);
    Optional<PublicQuery> findByIdAndSocietyId(Long id, Long societyId);
}
