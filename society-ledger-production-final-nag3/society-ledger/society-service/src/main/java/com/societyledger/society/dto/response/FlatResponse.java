package com.societyledger.society.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FlatResponse {
    private Long id;
    private Long societyId;
    private Long wingId;
    private String wingName;
    private String flatNumber;
    private Integer floorNumber;
    private BigDecimal areaSqft;
    private String paymentReferenceCode;
    private Boolean isOccupied;
    private String primaryResidentName;
    private Instant createdAt;
}
