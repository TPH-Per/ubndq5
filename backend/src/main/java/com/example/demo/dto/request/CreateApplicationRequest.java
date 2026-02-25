package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateApplicationRequest {

    @NotNull(message = "Procedure ID is required")
    private Integer procedureId;

    // Citizen info - can be existing or new
    // If existing citizen, provide citizenId (CCCD)
    @Size(min = 12, max = 12, message = "Citizen ID (CCCD) must be exactly 12 characters")
    private String citizenId;

    // If new citizen, provide these:
    private String citizenName;
    private LocalDate citizenDateOfBirth;
    private String citizenGender;
    private String citizenAddress;
    private String citizenPhone;
    private String citizenEmail;

    // Zalo info (optional)
    private Integer zaloAccountId;
    private String zaloId;
    private String zaloName;

    // Queue info
    private LocalDate appointmentDate;
    private LocalTime expectedTime;

    // Priority
    private Integer priority = 0;
}
