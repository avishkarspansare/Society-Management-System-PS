package com.societyledger.society.dto.response;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FamilyMemberResponse {
    private Long id;
    private Long flatId;
    private String fullName;
    private String relation;
    private LocalDate dateOfBirth;
    private String phone;
    private Boolean isPrimary;
    private Instant createdAt;
}
