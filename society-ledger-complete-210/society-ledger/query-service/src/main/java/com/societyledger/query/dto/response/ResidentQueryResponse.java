package com.societyledger.query.dto.response;

import com.societyledger.query.entity.ResidentQuery;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class ResidentQueryResponse {
    Long id;
    Long societyId;
    Long flatId;
    Long askedByUserId;
    String subject;
    String body;
    String answer;
    Long answeredBy;
    Instant answeredAt;
    ResidentQuery.QueryStatus status;
    Instant createdAt;
}
