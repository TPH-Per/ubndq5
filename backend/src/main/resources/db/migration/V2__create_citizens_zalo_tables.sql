-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V2
-- Citizens & Zalo Accounts Tables
-- =============================================

-- =============================================
-- CITIZENS - Công dân (Login qua Zalo)
-- =============================================
CREATE TABLE Citizens (
    CCCD VARCHAR(12) PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    DiaChiThuongTru TEXT,
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat TIMESTAMP
);

COMMENT ON TABLE Citizens IS 'Công dân - Người dân sử dụng dịch vụ, login qua Zalo';

-- Indexes for Citizens
CREATE INDEX idx_citizens_phone ON Citizens(SoDienThoai);
CREATE INDEX idx_citizens_hoten ON Citizens(HoTen);
CREATE INDEX idx_citizens_email ON Citizens(Email);

-- =============================================
-- ZALOACCOUNTS - Tài khoản Zalo liên kết
-- =============================================
CREATE TABLE ZaloAccounts (
    Id SERIAL PRIMARY KEY,
    ZaloId VARCHAR(100) UNIQUE NOT NULL,
    CCCD VARCHAR(12) NOT NULL,
    ZaloName VARCHAR(100),
    ZaloAvatar VARCHAR(500),
    NgayLienKet TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LanDangNhapCuoi TIMESTAMP,
    TrangThai BOOLEAN DEFAULT TRUE,
    CONSTRAINT FK_ZaloAccounts_Citizens FOREIGN KEY (CCCD) REFERENCES Citizens(CCCD) ON DELETE CASCADE
);

COMMENT ON TABLE ZaloAccounts IS 'Tài khoản Zalo liên kết với CCCD của công dân';

-- Indexes for ZaloAccounts
CREATE INDEX idx_zaloaccounts_cccd ON ZaloAccounts(CCCD);
CREATE INDEX idx_zaloaccounts_trangthai ON ZaloAccounts(TrangThai) WHERE TrangThai = TRUE;
