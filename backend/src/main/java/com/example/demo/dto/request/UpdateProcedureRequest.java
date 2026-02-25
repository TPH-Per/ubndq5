package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProcedureRequest {

    @Size(max = 200, message = "Procedure name must be less than 200 characters")
    private String procedureName;

    private String description;

    @Min(value = 1, message = "Processing days must be at least 1")
    private Integer processingDays;

    private String formSchema;

    private String requiredDocuments;

    private Integer displayOrder;

    private Boolean isActive;

    private Integer procedureTypeId;
}
