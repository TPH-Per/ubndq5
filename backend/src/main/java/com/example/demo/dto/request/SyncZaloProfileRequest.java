package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SyncZaloProfileRequest {

    @NotBlank(message = "Thiếu zaloId")
    private String zaloId;

    private String zaloName;

    private String avatar;

    private String oaUserId;

    private String phoneNumber;
}
