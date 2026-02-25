package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProcedureRequest {

    @NotBlank(message = "Procedure code is required")
    @Size(max = 20, message = "Procedure code must be less than 20 characters")
    private String procedureCode;

    @NotBlank(message = "Procedure name is required")
    @Size(max = 200, message = "Procedure name must be less than 200 characters")
    private String procedureName;

    private String description;

    @Min(value = 1, message = "Processing days must be at least 1")
    private Integer processingDays = 15;

    private String formSchema;

    private String requiredDocuments;

    private Integer displayOrder = 0;

    private Integer procedureTypeId;
}
