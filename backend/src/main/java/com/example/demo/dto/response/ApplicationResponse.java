package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Integer id;
    private String applicationCode;

    // Procedure info
    private Integer procedureId;
    private String procedureCode;
    private String procedureName;

    // Citizen info (citizenId = CCCD)
    private String citizenId;
    private String citizenName;
    private String citizenPhone;

    // Zalo info
    private Integer zaloAccountId;
    private String zaloName;

    // Phase
    private Integer currentPhase;
    private String phaseName;

    // Queue info
    private Integer queueNumber;
    private String queuePrefix;
    private String queueDisplay;
    private LocalDate appointmentDate;
    private LocalTime expectedTime;

    // Processing info
    private LocalDate deadline;
    private Integer priority;
    private String priorityName;

    // Cancellation
    private String cancelReason;
    private Integer cancelType;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method to get phase name
    public static String getPhaseName(Integer phase) {
        if (phase == null)
            return "Unknown";
        return switch (phase) {
            case 0 -> "Cancelled";
            case 1 -> "Queue";
            case 2 -> "Pending";
            case 3 -> "Processing";
            case 4 -> "Completed";
            case 5 -> "Received";
            default -> "Unknown";
        };
    }

    public static String getPriorityName(Integer priority) {
        if (priority == null)
            return "Normal";
        return switch (priority) {
            case 1 -> "Priority";
            case 2 -> "Urgent";
            default -> "Normal";
        };
    }
}
