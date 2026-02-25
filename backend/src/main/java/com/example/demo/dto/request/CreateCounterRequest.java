package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCounterRequest {

    @NotBlank(message = "Mã quầy không được để trống")
    @JsonAlias({ "maQuay", "counterCode" })
    private String maQuay;

    @NotBlank(message = "Tên quầy không được để trống")
    @JsonAlias({ "tenQuay", "counterName" })
    private String tenQuay;

    @JsonAlias({ "viTri", "location" })
    private String viTri;

    @JsonAlias({ "chuyenMonId", "procedureTypeId" })
    private Integer chuyenMonId;

    @JsonAlias({ "ghiChu", "notes" })
    private String ghiChu;
}
