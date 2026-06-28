package com.societyledger.society.dto.request;
import com.societyledger.society.entity.FamilyMember;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class CreateFamilyMemberRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotNull(message = "Relation is required")
    private FamilyMember.Relation relation;
    private LocalDate dateOfBirth;
    private String phone;
    private Boolean isPrimary;
}
