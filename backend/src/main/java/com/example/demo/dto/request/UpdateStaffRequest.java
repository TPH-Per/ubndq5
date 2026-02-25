package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStaffRequest {

    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 15, message = "Phone must be less than 15 characters")
    private String phone;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional: only update if provided

    private Integer roleId;

    private Integer counterId;

    private Boolean isActive;
}
