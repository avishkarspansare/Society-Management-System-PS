package com.societyledger.society.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class SocietyResponse {
    Long id;
    String societyName;
    String registrationNumber;
    String address;
    String city;
    String state;
    String pinCode;
    String contactEmail;
    String contactPhone;
    String subscriptionPlan;
    Boolean isActive;
    Instant createdAt;
}
