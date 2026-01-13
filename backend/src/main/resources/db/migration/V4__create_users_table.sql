-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V4
-- Users Table (Admin + Nhân viên)
-- =============================================

-- =============================================
-- USERS - Nhân viên hệ thống (Admin + NhanVien)
-- Login via Email/Password
-- =============================================
CREATE TABLE Users (
    Id SERIAL PRIMARY KEY,
    MaNhanVien VARCHAR(20) UNIQUE NOT NULL,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    SoDienThoai VARCHAR(15),
    PasswordHash VARCHAR(500) NOT NULL,
    PasswordSalt VARCHAR(500),
    RoleId INT NOT NULL,
    QuayId INT,
    TrangThai BOOLEAN DEFAULT TRUE,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NguoiTaoId INT,
    LanDangNhapCuoi TIMESTAMP,
    CONSTRAINT FK_Users_Role FOREIGN KEY (RoleId) REFERENCES Roles(Id),
    CONSTRAINT FK_Users_Quay FOREIGN KEY (QuayId) REFERENCES Quay(Id),
    CONSTRAINT FK_Users_NguoiTao FOREIGN KEY (NguoiTaoId) REFERENCES Users(Id)
);

COMMENT ON TABLE Users IS 'Nhân viên hệ thống (Admin + NhanVien), login qua Email/Password';

-- Indexes for Users
CREATE INDEX idx_users_email ON Users(Email) WHERE TrangThai = TRUE;
CREATE INDEX idx_users_role ON Users(RoleId);
CREATE INDEX idx_users_quay ON Users(QuayId) WHERE TrangThai = TRUE;
CREATE INDEX idx_users_manhanvien ON Users(MaNhanVien);
