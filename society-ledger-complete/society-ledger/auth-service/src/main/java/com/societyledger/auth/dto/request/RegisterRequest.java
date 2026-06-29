package com.societyledger.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

// ---------- RegisterRequest ----------
@Data
class RegisterRequest {
    @NotNull private Long societyId;
    @NotNull private Long flatId;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 8, max = 64) private String password;
    @NotBlank private String confirmPassword;
}
