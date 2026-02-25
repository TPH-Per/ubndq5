package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponse {
    private Integer id;
    private String staffCode;
    private String fullName;
    private String email;
    private String phone;
    private Integer roleId;
    private String roleName;
    private String roleDisplayName;
    private Integer counterId;
    private String counterName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
