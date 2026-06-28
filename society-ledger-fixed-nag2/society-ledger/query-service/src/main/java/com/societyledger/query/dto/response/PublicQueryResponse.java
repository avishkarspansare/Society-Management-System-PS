package com.societyledger.query.dto.response;
import lombok.*;
import java.time.Instant;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicQueryResponse {
    private Long id;
    private Long societyId;
    private Long flatId;
    private Long askedBy;
    private String subject;
    private String body;
    private String status;
    private List<QueryResponseDto> responses;
    private Instant createdAt;
    private Instant updatedAt;
}
