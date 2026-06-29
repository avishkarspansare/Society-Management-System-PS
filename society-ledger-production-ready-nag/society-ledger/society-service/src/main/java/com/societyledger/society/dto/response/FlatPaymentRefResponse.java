package com.societyledger.society.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FlatPaymentRefResponse {
    private Long flatId;
    private Long societyId;
    private String flatNumber;
    private String wingName;
    private String paymentReferenceCode;
    private String primaryResidentName;
    private String email;
}
