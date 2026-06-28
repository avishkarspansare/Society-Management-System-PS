package com.societyledger.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull(message = "Society ID is required")
    private Long societyId;

    @NotNull(message = "Flat ID is required")
    private Long flatId;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
