package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcedureTypeRequest {

    @NotBlank(message = "Mã chuyên môn không được để trống")
    @JsonAlias({ "maChuyenMon", "code" })
    private String maChuyenMon;

    @NotBlank(message = "Tên chuyên môn không được để trống")
    @JsonAlias({ "tenChuyenMon", "name" })
    private String tenChuyenMon;

    @JsonAlias({ "moTa", "description" })
    private String moTa;
}
