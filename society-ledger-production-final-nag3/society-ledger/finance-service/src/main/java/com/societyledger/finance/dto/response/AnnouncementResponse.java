package com.societyledger.finance.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private Long societyId;
    private Long createdBy;
    private String title;
    private String body;
    private String category;
    private Boolean isActive;
    private Instant createdAt;
    private Instant expiresAt;
}
