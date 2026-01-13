-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V8
-- AuditLog Table
-- =============================================

-- =============================================
-- AUDITLOG - Nhật ký hoạt động của nhân viên
-- =============================================
CREATE TABLE AuditLog (
    Id SERIAL PRIMARY KEY,
    UserId INT,
    HanhDong VARCHAR(50) NOT NULL,
    BangDuLieu VARCHAR(50),
    RecordId INT,
    DuLieuCu JSONB,
    DuLieuMoi JSONB,
    ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IPAddress VARCHAR(50),
    CONSTRAINT FK_AuditLog_Users FOREIGN KEY (UserId) REFERENCES Users(Id)
);

COMMENT ON TABLE AuditLog IS 'Audit log của nhân viên (KHÔNG log hành động công dân)';

-- Indexes for AuditLog
CREATE INDEX idx_auditlog_user ON AuditLog(UserId);
CREATE INDEX idx_auditlog_thoigian ON AuditLog(ThoiGian DESC);
CREATE INDEX idx_auditlog_bangdulieu ON AuditLog(BangDuLieu, RecordId);
