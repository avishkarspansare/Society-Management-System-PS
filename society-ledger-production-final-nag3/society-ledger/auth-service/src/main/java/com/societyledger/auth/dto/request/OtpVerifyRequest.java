package com.societyledger.auth.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class OtpVerifyRequest {
    @Email @NotBlank private String email;
    @NotBlank @Pattern(regexp = "\\d{6}") private String otpCode;
}
