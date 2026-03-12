package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateAppointmentRequest {

    @NotNull(message = "Thiếu procedureId")
    private Integer procedureId;

    @NotBlank(message = "Thiếu ngày hẹn")
    private String appointmentDate;

    @NotBlank(message = "Thiếu giờ hẹn")
    private String appointmentTime;

    @NotBlank(message = "Thiếu CCCD")
    @Pattern(regexp = "\\d{12}", message = "CCCD phải có đúng 12 số")
    private String citizenCccd;

    private String citizenName;
    private String citizenPhone;
    private String citizenEmail;

    @NotBlank(message = "Cần đăng nhập Zalo để đặt lịch hẹn")
    private String zaloId;

    private String zaloName;

    private String notes; // Ghi chú/mô tả từ công dân (không bắt buộc)
}
