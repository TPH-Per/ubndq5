package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenResponse {
    private String citizenId; // CCCD as Primary Key
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
