package com.societyledger.finance.service;

import com.societyledger.finance.entity.TransparencyTimeline;
import com.societyledger.finance.repository.TransparencyTimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TransparencyTimelineRepository timelineRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long societyId, String eventType, Long actorUserId,
                       String summary, String referenceType, Long referenceId) {
        try {
            TransparencyTimeline event = TransparencyTimeline.builder()
                    .societyId(societyId)
                    .eventType(eventType)
                    .eventSummary(summary)
                    .actorUserId(actorUserId)
                    .referenceType(referenceType)
                    .referenceId(referenceId)
                    .build();
            timelineRepository.save(event);
            log.debug("Timeline event recorded: {} for society {}", eventType, societyId);
        } catch (Exception e) {
            log.error("Failed to record timeline event {} for society {}: {}",
                    eventType, societyId, e.getMessage());
        }
    }
}
