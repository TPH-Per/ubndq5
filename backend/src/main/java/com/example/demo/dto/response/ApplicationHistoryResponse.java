package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryResponse {
    private Integer id;
    private Integer applicationId;
    private String applicationCode;

    // Counter info
    private Integer counterId;
    private String counterName;

    // Staff info
    private Integer staffId;
    private String staffName;

    // Phase change
    private Integer phaseFrom;
    private String phaseFromName;
    private Integer phaseTo;
    private String phaseToName;

    // Action
    private String action;
    private String content;

    // Form data
    private Map<String, Object> formData;
    private Map<String, Object> attachments;

    private LocalDateTime createdAt;
}
