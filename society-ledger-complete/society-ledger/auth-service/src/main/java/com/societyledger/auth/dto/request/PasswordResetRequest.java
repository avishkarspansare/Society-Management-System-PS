package com.societyledger.auth.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class PasswordResetRequest {
    @Email @NotBlank private String email;
    @NotBlank @Pattern(regexp = "\\d{6}") private String otpCode;
    @NotBlank @Size(min = 8, max = 64) private String newPassword;
    @NotBlank private String confirmPassword;
}
