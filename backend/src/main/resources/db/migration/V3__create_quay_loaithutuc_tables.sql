-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V3
-- Quay & LoaiThuTuc Tables
-- =============================================

-- =============================================
-- QUAY - Quầy giao dịch
-- =============================================
CREATE TABLE Quay (
    Id SERIAL PRIMARY KEY,
    MaQuay VARCHAR(10) UNIQUE NOT NULL,
    TenQuay VARCHAR(50) NOT NULL,
    ViTri VARCHAR(100),
    PrefixSo VARCHAR(5) NOT NULL,
    ChuyenMonId INT NOT NULL,
    TrangThai BOOLEAN DEFAULT TRUE,
    GhiChu TEXT,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Quay_ChuyenMon FOREIGN KEY (ChuyenMonId) REFERENCES ChuyenMon(Id)
);

COMMENT ON TABLE Quay IS 'Quầy giao dịch, mỗi quầy thuộc 1 chuyên môn';

-- Indexes for Quay
CREATE INDEX idx_quay_chuyenmon ON Quay(ChuyenMonId) WHERE TrangThai = TRUE;

-- =============================================
-- LOAITHUTUC - Loại thủ tục hành chính
-- =============================================
CREATE TABLE LoaiThuTuc (
    Id SERIAL PRIMARY KEY,
    MaThuTuc VARCHAR(20) UNIQUE NOT NULL,
    TenThuTuc VARCHAR(200) NOT NULL,
    MoTa TEXT,
    ChuyenMonId INT NOT NULL,
    ThoiGianXuLy INT NOT NULL DEFAULT 15,
    FormSchema JSONB,
    GiayToYeuCau TEXT,
    TrangThai BOOLEAN DEFAULT TRUE,
    ThuTu INT DEFAULT 0,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LoaiThuTuc_ChuyenMon FOREIGN KEY (ChuyenMonId) REFERENCES ChuyenMon(Id)
);

COMMENT ON TABLE LoaiThuTuc IS 'Các loại thủ tục hành chính';

-- Indexes for LoaiThuTuc
CREATE INDEX idx_loaithutuc_chuyenmon ON LoaiThuTuc(ChuyenMonId) WHERE TrangThai = TRUE;
