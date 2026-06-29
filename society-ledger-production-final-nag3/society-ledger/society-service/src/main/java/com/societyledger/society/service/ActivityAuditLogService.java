package com.societyledger.society.service;

import com.societyledger.society.entity.ActivityAuditLog;
import com.societyledger.society.repository.ActivityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityAuditLogService {

    private final ActivityAuditLogRepository repository;

    /**
     * Log an activity asynchronously so it never blocks the main transaction.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long societyId, Long userId, String action,
                    String entityType, Long entityId, String description) {
        try {
            ActivityAuditLog entry = ActivityAuditLog.builder()
                    .societyId(societyId)
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();
            repository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log for action {} society {}: {}",
                    action, societyId, e.getMessage());
        }
    }
}
