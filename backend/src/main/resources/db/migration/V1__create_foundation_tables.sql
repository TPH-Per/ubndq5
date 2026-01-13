-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V1
-- Foundation Tables: Roles, ChuyenMon, HeThongCauHinh
-- =============================================

SET timezone = 'Asia/Ho_Chi_Minh';

-- =============================================
-- ROLES - Vai trò người dùng
-- =============================================
CREATE TABLE Roles (
    Id SERIAL PRIMARY KEY,
    RoleName VARCHAR(50) UNIQUE NOT NULL,
    DisplayName VARCHAR(100) NOT NULL,
    MoTa TEXT
);

COMMENT ON TABLE Roles IS 'Vai trò: CHỈ Admin và NhanVien (KHÔNG có CongDan)';

-- =============================================
-- CHUYENMON - Chuyên môn nghiệp vụ
-- =============================================
CREATE TABLE ChuyenMon (
    Id SERIAL PRIMARY KEY,
    MaChuyenMon VARCHAR(20) UNIQUE NOT NULL,
    TenChuyenMon VARCHAR(100) NOT NULL,
    MoTa TEXT,
    TrangThai BOOLEAN DEFAULT TRUE,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ChuyenMon IS 'Chuyên môn nghiệp vụ (Dân số, Kinh doanh, Đất đai, Xây dựng)';

-- =============================================
-- HETHONGCAUHINH - Cấu hình hệ thống
-- =============================================
CREATE TABLE HeThongCauHinh (
    Id SERIAL PRIMARY KEY,
    ConfigKey VARCHAR(50) UNIQUE NOT NULL,
    ConfigValue TEXT,
    MoTa TEXT,
    NgayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE HeThongCauHinh IS 'Bảng cấu hình hệ thống';
