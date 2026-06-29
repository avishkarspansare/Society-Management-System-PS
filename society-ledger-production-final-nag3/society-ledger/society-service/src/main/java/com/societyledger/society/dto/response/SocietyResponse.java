package com.societyledger.society.dto.response;
import lombok.*;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SocietyResponse {
    private Long id;
    private String societyName;
    private String registrationNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactEmail;
    private String contactPhone;
    private String planName;
    private Boolean isActive;
    private Instant createdAt;
}
