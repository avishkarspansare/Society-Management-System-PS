package com.societyledger.query.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.query.dto.request.CreateQueryRequest;
import com.societyledger.query.entity.PublicQuery;
import com.societyledger.query.repository.PublicQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicQueryService Unit Tests")
class PublicQueryServiceTest {

    @Mock PublicQueryRepository queryRepository;
    @Mock org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks PublicQueryService publicQueryService;

    private PublicQuery openQuery;

    @BeforeEach
    void setUp() {
        openQuery = PublicQuery.builder()
                .id(1L)
                .societyId(10L)
                .submittedByUserId(5L)
                .title("Why was Rs 5000 charged for maintenance?")
                .body("Please clarify the June 2024 maintenance charge.")
                .status(PublicQuery.QueryStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("createQuery persists and returns query")
    void createQuery_persistsQuery() {
        var request = new CreateQueryRequest();
        request.setTitle("Test question");
        request.setBody("Test body for question");

        when(queryRepository.save(any(PublicQuery.class))).thenAnswer(inv -> {
            var q = inv.getArgument(0, PublicQuery.class);
            q = PublicQuery.builder()
                    .id(2L).societyId(q.getSocietyId())
                    .submittedByUserId(q.getSubmittedByUserId())
                    .title(q.getTitle()).body(q.getBody())
                    .status(PublicQuery.QueryStatus.OPEN).build();
            return q;
        });

        var result = publicQueryService.createQuery(10L, 5L, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test question");
        verify(queryRepository).save(any(PublicQuery.class));
    }

    @Test
    @DisplayName("getQueryById throws NOT_FOUND for unknown query")
    void getQueryById_throwsForUnknown() {
        when(queryRepository.findByIdAndSocietyId(eq(99L), eq(10L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicQueryService.getQueryById(10L, 99L))
                .isInstanceOf(SocietyLedgerException.class);
    }

    @Test
    @DisplayName("respondToQuery updates status to ANSWERED")
    void respondToQuery_setsAnsweredStatus() {
        when(queryRepository.findByIdAndSocietyId(eq(1L), eq(10L)))
                .thenReturn(Optional.of(openQuery));
        when(queryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new com.societyledger.query.dto.request.RespondToQueryRequest();
        req.setResponse("The Rs 5000 includes water tax and sinking fund as per AGM resolution.");

        var result = publicQueryService.respondToQuery(10L, 1L, 99L, req);

        assertThat(result.getStatus()).isEqualTo("ANSWERED");
        assertThat(result.getResponse()).contains("water tax");
    }
}
