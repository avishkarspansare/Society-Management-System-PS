package com.societyledger.statement.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FlatPaymentRefDto {
    private Long flatId;
    private Long societyId;
    private String flatNumber;
    private String wingName;
    private String paymentReferenceCode;
    private String primaryResidentName;
    private String email;
}
