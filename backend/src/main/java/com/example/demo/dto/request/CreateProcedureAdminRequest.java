package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProcedureAdminRequest {

    @NotBlank(message = "Mã thủ tục không được để trống")
    @JsonAlias({ "maThuTuc", "procedureCode" })
    private String maThuTuc;

    @NotBlank(message = "Tên thủ tục không được để trống")
    @JsonAlias({ "tenThuTuc", "procedureName" })
    private String tenThuTuc;

    @JsonAlias({ "moTa", "description" })
    private String moTa;

    @NotNull(message = "Chuyên môn không được để trống")
    @JsonAlias({ "chuyenMonId", "procedureTypeId" })
    private Integer chuyenMonId;

    @JsonAlias({ "thoiGianXuLy", "processingDays" })
    private Integer thoiGianXuLy;

    @JsonAlias({ "giayToYeuCau", "requiredDocuments" })
    private String giayToYeuCau;

    @JsonAlias({ "formSchema" })
    private String formSchema;

    @JsonAlias({ "thuTu", "displayOrder" })
    private Integer thuTu;
}
