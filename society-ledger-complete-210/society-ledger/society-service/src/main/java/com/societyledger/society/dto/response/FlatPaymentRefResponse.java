package com.societyledger.society.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class FlatPaymentRefResponse {
    Long flatId;
    Long societyId;
    String flatNumber;
    String wingName;
    String paymentReferenceCode;
    String primaryResidentName;
    String primaryResidentEmail;
}
