package com.societyledger.society.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSocietyRequest {
    @NotBlank String societyName;
    String registrationNumber;
    String address;
    String city;
    String state;
    String pinCode;
    @Email String contactEmail;
    String contactPhone;
}
