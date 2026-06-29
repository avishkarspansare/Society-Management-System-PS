package com.societyledger.auth.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String role;
    private Long societyId;
    private Long flatId;
    private Long userId;
    private String email;
}
