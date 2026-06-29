package com.societyledger.query.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RespondToQueryRequest {
    @NotBlank(message="Response text is required") private String response;
}