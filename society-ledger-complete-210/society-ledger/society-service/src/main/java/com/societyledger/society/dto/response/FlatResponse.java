package com.societyledger.society.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class FlatResponse {
    Long id;
    Long societyId;
    Long wingId;
    String wingName;
    String flatNumber;
    Integer floorNumber;
    BigDecimal areaSqft;
    String paymentReferenceCode;
    Boolean isOccupied;
    String primaryResidentName;
    Instant createdAt;
}
