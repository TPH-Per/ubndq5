package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCounterRequest {

    @JsonAlias({ "tenQuay", "counterName" })
    private String tenQuay;

    @JsonAlias({ "viTri", "location" })
    private String viTri;

    @JsonAlias({ "chuyenMonId", "procedureTypeId" })
    private Integer chuyenMonId;

    @JsonAlias({ "ghiChu", "notes" })
    private String ghiChu;

    @JsonAlias({ "trangThai", "isActive" })
    private Boolean trangThai;
}
