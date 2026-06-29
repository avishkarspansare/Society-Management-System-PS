package com.societyledger.society.repository;
import com.societyledger.society.entity.ActivityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ActivityAuditLogRepository extends JpaRepository<ActivityAuditLog, Long> {
    Page<ActivityAuditLog> findBySocietyIdOrderByOccurredAtDesc(Long societyId, Pageable pageable);
}
