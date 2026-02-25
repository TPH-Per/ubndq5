package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStaffRequest {

    @NotBlank(message = "Staff code is required")
    @Size(max = 20, message = "Staff code must be less than 20 characters")
    private String staffCode;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 15, message = "Phone must be less than 15 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role ID is required")
    private Integer roleId;

    private Integer counterId;
}
