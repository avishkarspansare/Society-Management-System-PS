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

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long societyId, Long actorUserId, String action,
                    String entityType, Long entityId, String description) {
        try {
            repository.save(ActivityAuditLog.builder()
                    .societyId(societyId)
                    .actorUserId(actorUserId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build());
        } catch (Exception e) {
            log.error("Failed to write audit log for society {}: {}", societyId, e.getMessage());
        }
    }
}
