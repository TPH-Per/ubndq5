package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned when performing "Complete & Call Next" action
 * 
 * Contains:
 * - Just completed application
 * - Next application (if any)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAndCallNextResponse {

    /**
     * Info of just completed application
     */
    private ApplicationResponse completedApplication;

    /**
     * Info of next application called
     * Null if queue is empty
     */
    private ApplicationResponse nextApplication;

    /**
     * Summary message for frontend display
     */
    private String message;

    /**
     * Is there a next application?
     */
    private boolean hasNext;
}
