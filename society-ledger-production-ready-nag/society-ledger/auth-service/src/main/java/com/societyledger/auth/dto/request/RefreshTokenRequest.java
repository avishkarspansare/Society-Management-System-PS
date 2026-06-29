package com.societyledger.auth.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RefreshTokenRequest {
    @NotBlank private String refreshToken;
}
