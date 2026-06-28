package com.societyledger.society.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WingResponse {
    private Long id;
    private Long societyId;
    private String wingName;
    private int flatCount;
    private Instant createdAt;
}
