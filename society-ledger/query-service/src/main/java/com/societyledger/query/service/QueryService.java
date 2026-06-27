package com.societyledger.query.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.common.security.JwtClaims;
import com.societyledger.query.dto.request.AnswerQueryRequest;
import com.societyledger.query.dto.request.CreateQueryRequest;
import com.societyledger.query.dto.response.ResidentQueryResponse;
import com.societyledger.query.entity.ResidentQuery;
import com.societyledger.query.kafka.QueryEventProducer;
import com.societyledger.query.repository.ResidentQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final ResidentQueryRepository repository;
    private final QueryEventProducer eventProducer;

    @Transactional
    public ResidentQueryResponse createQuery(Long societyId, CreateQueryRequest req, JwtClaims claims) {
        ResidentQuery saved = repository.save(ResidentQuery.builder()
                .societyId(societyId)
                .flatId(claims.getFlatId())
                .askedByUserId(claims.getUserId())
                .subject(req.getSubject().strip())
                .body(req.getBody().strip())
                .status(ResidentQuery.QueryStatus.OPEN)
                .build());
        log.info("Query created {} by user {} in society {}", saved.getId(), claims.getUserId(), societyId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ResidentQueryResponse> getQueries(Long societyId, String status,
                                                           String role, Long userId, Pageable pageable) {
        if ("RESIDENT".equals(role)) {
            return PageResponse.from(
                    repository.findBySocietyIdAndAskedByUserIdOrderByCreatedAtDesc(societyId, userId, pageable)
                              .map(this::mapToResponse));
        }
        if (status != null) {
            ResidentQuery.QueryStatus qs = ResidentQuery.QueryStatus.valueOf(status.toUpperCase());
            return PageResponse.from(
                    repository.findBySocietyIdAndStatusOrderByCreatedAtDesc(societyId, qs, pageable)
                              .map(this::mapToResponse));
        }
        return PageResponse.from(
                repository.findBySocietyIdOrderByCreatedAtDesc(societyId, pageable)
                          .map(this::mapToResponse));
    }

    @Transactional
    public ResidentQueryResponse answerQuery(Long societyId, Long queryId,
                                              AnswerQueryRequest req, Long adminUserId) {
        ResidentQuery q = repository.findByIdAndSocietyId(queryId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Query", queryId));
        if (q.getStatus() == ResidentQuery.QueryStatus.CLOSED)
            throw new SocietyLedgerException("Query is already closed.", "QUERY_CLOSED", HttpStatus.CONFLICT);

        q.setAnswer(req.getAnswer().strip());
        q.setAnsweredBy(adminUserId);
        q.setAnsweredAt(Instant.now());
        q.setStatus(ResidentQuery.QueryStatus.ANSWERED);
        ResidentQuery saved = repository.save(q);

        eventProducer.publishQueryAnswered(societyId, queryId, q.getAskedByUserId());
        return mapToResponse(saved);
    }

    @Transactional
    public ResidentQueryResponse closeQuery(Long societyId, Long queryId, Long adminUserId) {
        ResidentQuery q = repository.findByIdAndSocietyId(queryId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Query", queryId));
        q.setStatus(ResidentQuery.QueryStatus.CLOSED);
        return mapToResponse(repository.save(q));
    }

    private ResidentQueryResponse mapToResponse(ResidentQuery q) {
        return ResidentQueryResponse.builder()
                .id(q.getId()).societyId(q.getSocietyId()).flatId(q.getFlatId())
                .askedByUserId(q.getAskedByUserId()).subject(q.getSubject()).body(q.getBody())
                .answer(q.getAnswer()).answeredBy(q.getAnsweredBy()).answeredAt(q.getAnsweredAt())
                .status(q.getStatus()).createdAt(q.getCreatedAt())
                .build();
    }
}
