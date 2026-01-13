package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response wrapper tổng quát cho tất cả API
 * 
 * Đảm bảo response format NHẤT QUÁN cho toàn bộ ứng dụng.
 * 
 * Ví dụ response thành công:
 * {
 *   "success": true,
 *   "code": "SUCCESS",
 *   "message": "Đăng nhập thành công",
 *   "data": { ... },
 *   "timestamp": "2026-01-06T01:30:00"
 * }
 * 
 * Ví dụ response lỗi:
 * {
 *   "success": false,
 *   "code": "AUTH_002",
 *   "message": "Mã nhân viên hoặc mật khẩu không đúng",
 *   "data": null,
 *   "timestamp": "2026-01-06T01:30:00"
 * }
 * 
 * @param <T> Kiểu dữ liệu của data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Không serialize fields null
public class ApiResponse<T> {
    
    /**
     * Trạng thái request: true = thành công, false = thất bại
     */
    private boolean success;
    
    /**
     * Mã code (SUCCESS hoặc error code từ ErrorCode enum)
     */
    private String code;
    
    /**
     * Thông báo chi tiết (tiếng Việt)
     */
    private String message;
    
    /**
     * Dữ liệu trả về (null nếu lỗi)
     */
    private T data;
    
    /**
     * Thời gian response
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    // ==================== STATIC FACTORY METHODS ====================
    
    /**
     * Tạo success response với data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Thành công")
                .data(data)
                .build();
    }
    
    /**
     * Tạo success response với data và message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Tạo success response không có data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .build();
    }
    
    /**
     * Tạo error response từ ErrorCode
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
    
    /**
     * Tạo error response từ exception
     */
    public static <T> ApiResponse<T> error(com.example.demo.exception.ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
}
