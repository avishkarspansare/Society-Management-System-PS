package com.societyledger.finance.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.finance.entity.FinancialTimeline;
import com.societyledger.finance.repository.FinancialTimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final FinancialTimelineRepository repository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long societyId, String eventType, Long actorUserId,
                       String description, String refEntityType, Long refEntityId) {
        repository.save(FinancialTimeline.builder()
                .societyId(societyId).eventType(eventType)
                .actorUserId(actorUserId).description(description)
                .refEntityType(refEntityType).refEntityId(refEntityId)
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<FinancialTimeline> getTimeline(Long societyId, Pageable pageable) {
        return PageResponse.from(
                repository.findBySocietyIdOrderByCreatedAtDesc(societyId, pageable));
    }
}
