package com.societyledger.audit.repository;

import com.societyledger.audit.entity.AuditReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuditReportRepository extends JpaRepository<AuditReport, Long> {
    Page<AuditReport> findBySocietyIdOrderByAuditYearDescUploadedAtDesc(Long societyId, Pageable p);
    Optional<AuditReport> findByIdAndSocietyId(Long id, Long societyId);
}
