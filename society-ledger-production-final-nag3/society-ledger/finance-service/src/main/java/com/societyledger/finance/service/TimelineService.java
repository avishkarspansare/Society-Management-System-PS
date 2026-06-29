package com.societyledger.finance.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.finance.dto.response.TimelineResponse;
import com.societyledger.finance.entity.TransparencyTimeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public PageResponse<TimelineResponse> getTimeline(Long societyId, int page, int size) {
        Page<TransparencyTimeline> pageResult = timelineRepository
                .findBySocietyIdOrderByOccurredAtDesc(societyId,
                        PageRequest.of(page, size, Sort.by("occurredAt").descending()));
        return PageResponse.<TimelineResponse>builder()
                .content(pageResult.getContent().stream()
                        .map(t -> TimelineResponse.builder()
                                .id(t.getId())
                                .societyId(t.getSocietyId())
                                .eventType(t.getEventType())
                                .eventSummary(t.getEventSummary())
                                .actorUserId(t.getActorUserId())
                                .referenceId(t.getReferenceId())
                                .referenceType(t.getReferenceType())
                                .occurredAt(t.getOccurredAt())
                                .build())
                        .toList())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

}