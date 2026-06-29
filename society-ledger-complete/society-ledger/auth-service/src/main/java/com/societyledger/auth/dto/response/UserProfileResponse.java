package com.societyledger.auth.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String email;
    private String role;
    private Long societyId;
    private Long flatId;
    private Boolean isActive;
}
