package com.example.demo.controller;

/**
 * Shared status name helpers for citizen controllers.
 * Extracted to avoid duplication across split controller files.
 */
public final class CitizenHelperUtils {

    private CitizenHelperUtils() {}

    /**
     * §6.2 CCCD handling — mask all but first 3 and last 3 digits.
     * Example: "012345678901" → "012******901"
     */
    public static String maskCccd(String cccd) {
        if (cccd == null || cccd.length() < 4) return "****";
        int visibleEnd = 3;
        int maskLen = Math.max(0, cccd.length() - visibleEnd * 2);
        return cccd.substring(0, visibleEnd)
                + "*".repeat(maskLen)
                + cccd.substring(cccd.length() - visibleEnd);
    }

    public static String getStatusName(Integer phase) {
        if (phase == null) return "---";
        return switch (phase) {
            case 0 -> "CANCELLED";
            case 1 -> "IN_QUEUE";
            case 2 -> "PENDING";
            case 3 -> "PROCESSING";
            case 4 -> "COMPLETED";
            case 5 -> "RECEIVED";
            case 6 -> "SUPPLEMENT";
            default -> "UNKNOWN";
        };
    }

    public static String getQueueStatusName(int phase) {
        return switch (phase) {
            case 0    -> "CANCELLED";
            case 1, 2 -> "WAITING";
            case 3    -> "CALLED";
            case 4, 5 -> "COMPLETED";
            case 6    -> "SUPPLEMENT";
            default   -> "UNKNOWN";
        };
    }

    public static String getReportStatusName(int status) {
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "PROCESSING";
            case 2 -> "RESOLVED";
            default -> "UNKNOWN";
        };
    }
}
