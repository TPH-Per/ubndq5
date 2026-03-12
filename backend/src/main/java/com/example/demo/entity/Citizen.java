package com.example.demo.entity;

/**
 * @deprecated Bảng Citizen đã được bỏ.
 * Thông tin công dân (cccd, tên, sđt, email) nay được lưu trực tiếp (inline)
 * trong entity {@link Application} và {@link Report}.
 * File này giữ lại để tránh lỗi compile nếu còn references cũ,
 * nhưng không được dùng trong logic mới.
 */
@Deprecated
public class Citizen {
    // Empty - replaced by inline fields in Application entity
    // citizen_cccd, citizen_name, citizen_phone, citizen_email
}
