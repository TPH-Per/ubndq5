package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcedureTypeRequest {

    @JsonAlias({ "tenChuyenMon", "name" })
    private String tenChuyenMon;

    @JsonAlias({ "moTa", "description" })
    private String moTa;

    @JsonAlias({ "trangThai", "isActive" })
    private Boolean trangThai;
}
