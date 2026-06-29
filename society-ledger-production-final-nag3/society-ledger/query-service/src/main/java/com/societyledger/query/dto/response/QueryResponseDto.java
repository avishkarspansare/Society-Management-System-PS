package com.societyledger.query.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QueryResponseDto {
    private Long id;
    private Long respondedBy;
    private String response;
    private Instant createdAt;
}
