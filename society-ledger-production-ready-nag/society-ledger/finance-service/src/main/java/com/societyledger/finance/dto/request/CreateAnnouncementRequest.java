package com.societyledger.finance.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.Instant;
@Data
public class CreateAnnouncementRequest {
    @NotBlank(message = "Title is required") @Size(max = 255) private String title;
    @NotBlank(message = "Body is required") private String body;
    private String category;
    private Instant expiresAt;
}
