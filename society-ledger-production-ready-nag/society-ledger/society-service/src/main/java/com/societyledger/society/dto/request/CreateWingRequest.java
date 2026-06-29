package com.societyledger.society.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreateWingRequest {
    @NotBlank(message = "Wing name is required")
    @Size(max = 50)
    private String wingName;
}
