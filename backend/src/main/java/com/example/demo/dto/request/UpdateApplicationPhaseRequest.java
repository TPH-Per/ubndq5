package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationPhaseRequest {

    @NotNull(message = "Application ID is required")
    private Integer applicationId;

    @NotNull(message = "Counter ID is required")
    private Integer counterId;

    @NotBlank(message = "Action is required")
    private String action; // CALL, REQUEST_DOCS, SUBMIT_DOCS, COMPLETE, CANCEL_*

    private String content; // Notes/reason

    private Map<String, Object> formData; // Form data if updating

    private Map<String, Object> attachments; // Attached files
}
