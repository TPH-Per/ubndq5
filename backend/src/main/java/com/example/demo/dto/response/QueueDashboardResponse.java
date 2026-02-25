package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for queue dashboard at counter
 * 
 * Returns for staff queue management screen, including:
 * - Counter info
 * - Currently processing (if any)
 * - Waiting list
 * - Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueDashboardResponse {

    // ==================== COUNTER INFO ====================

    /**
     * Counter ID
     */
    private Integer counterId;

    /**
     * Counter name (e.g., "Counter A - Civil Registration")
     */
    private String counterName;

    /**
     * Counter code
     */
    private String counterCode;

    // ==================== CURRENTLY PROCESSING ====================

    /**
     * Current processing application info
     * 
     * null if staff hasn't called anyone
     * Used for: Display "Serving: A001 - Nguyen Van A"
     */
    private ApplicationResponse currentProcessing;

    // ==================== WAITING LIST ====================

    /**
     * List of waiting applications
     * 
     * Sorted by queue number ascending
     * First person will be called next
     */
    private List<ApplicationResponse> waitingList;

    // ==================== STATISTICS ====================

    /**
     * Total number of people waiting
     */
    private Integer totalWaiting;

    /**
     * Number of completed today
     */
    private Integer totalCompleted;

    /**
     * Number of cancelled/no-show today
     */
    private Integer totalCancelled;

    /**
     * Average processing time (minutes)
     * Used for: Estimate waiting time for last person in queue
     */
    private Integer averageProcessingTime;
}
