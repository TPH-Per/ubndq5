package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureAdminRequest {

    @JsonAlias({ "tenThuTuc", "procedureName" })
    private String tenThuTuc;

    @JsonAlias({ "moTa", "description" })
    private String moTa;

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

    @JsonAlias({ "trangThai", "isActive" })
    private Boolean trangThai;
}
