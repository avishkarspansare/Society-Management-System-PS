package com.societyledger.society.repository;

import com.societyledger.society.entity.Wing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WingRepository extends JpaRepository<Wing, Long> {
    Optional<Wing> findByIdAndSocietyId(Long id, Long societyId);
    List<Wing> findBySocietyIdOrderByWingNameAsc(Long societyId);
    Page<Wing> findBySocietyId(Long societyId, Pageable pageable);
    boolean existsBySocietyIdAndWingName(Long societyId, String wingName);
}
