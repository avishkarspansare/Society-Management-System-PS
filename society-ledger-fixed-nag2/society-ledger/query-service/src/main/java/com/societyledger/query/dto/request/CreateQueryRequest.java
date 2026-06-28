package com.societyledger.query.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CreateQueryRequest {
    @NotBlank(message = "Subject is required") @Size(max = 255) private String subject;
    @NotBlank(message = "Question body is required") private String body;
}
