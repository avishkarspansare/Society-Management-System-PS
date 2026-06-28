package com.societyledger.finance.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpenseCategoryResponse {
    private Long id;
    private Long societyId;
    private String name;
    private String description;
    private Instant createdAt;
}
