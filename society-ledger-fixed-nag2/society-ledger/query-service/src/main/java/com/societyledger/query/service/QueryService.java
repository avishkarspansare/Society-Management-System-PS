package com.societyledger.query.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.query.dto.request.CreateQueryRequest;
import com.societyledger.query.dto.request.RespondToQueryRequest;
import com.societyledger.query.dto.response.PublicQueryResponse;
import com.societyledger.query.dto.response.QueryResponseDto;
import com.societyledger.query.entity.PublicQuery;
import com.societyledger.query.entity.QueryResponseEntity;
import com.societyledger.query.kafka.QueryEventProducer;
import com.societyledger.query.repository.PublicQueryRepository;
import com.societyledger.query.repository.QueryResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {

    private final PublicQueryRepository queryRepository;
    private final QueryResponseRepository responseRepository;
    private final QueryEventProducer eventProducer;

    @Transactional
    public PublicQueryResponse createQuery(Long societyId, Long flatId, Long userId,
                                            CreateQueryRequest request) {
        PublicQuery query = PublicQuery.builder()
                .societyId(societyId)
                .flatId(flatId)
                .askedBy(userId)
                .subject(request.getSubject().trim())
                .body(request.getBody().trim())
                .status(PublicQuery.QueryStatus.OPEN)
                .build();

        PublicQuery saved = queryRepository.save(query);
        log.info("Query created: {} by flat {} in society {}", saved.getId(), flatId, societyId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicQueryResponse> getQueries(Long societyId, Pageable pageable) {
        return PageResponse.from(
                queryRepository.findBySocietyIdOrderByCreatedAtDesc(societyId, pageable)
                        .map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PublicQueryResponse getQueryById(Long societyId, Long queryId) {
        return queryRepository.findByIdAndSocietyId(queryId, societyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("Query", queryId));
    }

    @Transactional
    public PublicQueryResponse respondToQuery(Long societyId, Long queryId, Long adminId,
                                               RespondToQueryRequest request) {
        PublicQuery query = queryRepository.findByIdAndSocietyId(queryId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Query", queryId));

        if (query.getStatus() == PublicQuery.QueryStatus.CLOSED) {
            throw new SocietyLedgerException("Cannot respond to a closed query.",
                    "QUERY_CLOSED", HttpStatus.BAD_REQUEST);
        }

        QueryResponseEntity response = QueryResponseEntity.builder()
                .query(query)
                .societyId(societyId)
                .respondedBy(adminId)
                .response(request.getResponse().trim())
                .build();

        responseRepository.save(response);

        query.setStatus(PublicQuery.QueryStatus.ANSWERED);
        queryRepository.save(query);

        log.info("Query {} answered by admin {} in society {}", queryId, adminId, societyId);

        // Publish Kafka event → notifies the resident who asked
        eventProducer.publishQueryAnswered(query);

        return mapToResponse(query);
    }

    @Transactional
    public PublicQueryResponse closeQuery(Long societyId, Long queryId) {
        PublicQuery query = queryRepository.findByIdAndSocietyId(queryId, societyId)
                .orElseThrow(() -> SocietyLedgerException.notFound("Query", queryId));
        query.setStatus(PublicQuery.QueryStatus.CLOSED);
        return mapToResponse(queryRepository.save(query));
    }

    private PublicQueryResponse mapToResponse(PublicQuery q) {
        List<QueryResponseDto> responses = responseRepository
                .findByQueryIdOrderByCreatedAtAsc(q.getId())
                .stream()
                .map(r -> QueryResponseDto.builder()
                        .id(r.getId())
                        .respondedBy(r.getRespondedBy())
                        .response(r.getResponse())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PublicQueryResponse.builder()
                .id(q.getId()).societyId(q.getSocietyId()).flatId(q.getFlatId())
                .askedBy(q.getAskedBy()).subject(q.getSubject()).body(q.getBody())
                .status(q.getStatus().name()).responses(responses)
                .createdAt(q.getCreatedAt()).updatedAt(q.getUpdatedAt())
                .build();
    }
}
