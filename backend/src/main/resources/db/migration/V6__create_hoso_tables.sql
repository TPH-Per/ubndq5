-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V6
-- HoSo & HoSoXuLy Tables (Document Management)
-- =============================================

-- =============================================
-- HOSO - Hồ sơ của công dân
-- =============================================
CREATE TABLE HoSo (
    Id SERIAL PRIMARY KEY,
    MaHoSo VARCHAR(20) UNIQUE NOT NULL,
    CCCD VARCHAR(12) NOT NULL,
    ZaloId VARCHAR(100),
    LoaiThuTucId INT NOT NULL,
    QuayId INT NOT NULL,
    ThongTinHoSo JSONB,
    FileDinhKem JSONB,
    TrangThai INT NOT NULL DEFAULT 0,
    DoUuTien INT DEFAULT 0,
    NgayNop TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayHoanThanh TIMESTAMP,
    HanXuLy DATE,
    LichHenId INT,
    GhiChu TEXT,
    CONSTRAINT FK_HoSo_Citizens FOREIGN KEY (CCCD) REFERENCES Citizens(CCCD),
    CONSTRAINT FK_HoSo_ZaloAccounts FOREIGN KEY (ZaloId) REFERENCES ZaloAccounts(ZaloId),
    CONSTRAINT FK_HoSo_LoaiThuTuc FOREIGN KEY (LoaiThuTucId) REFERENCES LoaiThuTuc(Id),
    CONSTRAINT FK_HoSo_Quay FOREIGN KEY (QuayId) REFERENCES Quay(Id),
    CONSTRAINT FK_HoSo_LichHen FOREIGN KEY (LichHenId) REFERENCES LichHen(Id)
);

COMMENT ON TABLE HoSo IS 'Hồ sơ của công dân, có thể từ lịch hẹn hoặc walk-in';
COMMENT ON COLUMN HoSo.TrangThai IS '0=Mới, 1=Đang XL, 2=Cần BS, 3=Chờ PD, 4=Hoàn thành, 5=Từ chối';
COMMENT ON COLUMN HoSo.DoUuTien IS '0=Bình thường, 1=Ưu tiên, 2=Khẩn cấp';

-- Indexes for HoSo
CREATE INDEX idx_hoso_cccd ON HoSo(CCCD);
CREATE INDEX idx_hoso_quay ON HoSo(QuayId);
CREATE INDEX idx_hoso_trangthai ON HoSo(TrangThai) WHERE TrangThai < 4;
CREATE INDEX idx_hoso_ngaynop ON HoSo(NgayNop DESC);
CREATE INDEX idx_hoso_hanxuly ON HoSo(HanXuLy) WHERE TrangThai < 4;
CREATE INDEX idx_hoso_lichhen ON HoSo(LichHenId) WHERE LichHenId IS NOT NULL;

-- =============================================
-- HOSOXULY - Workflow xử lý hồ sơ
-- =============================================
CREATE TABLE HoSoXuLy (
    Id SERIAL PRIMARY KEY,
    HoSoId INT NOT NULL,
    UserId INT NOT NULL,
    HanhDong VARCHAR(50) NOT NULL,
    TrangThaiCu INT,
    TrangThaiMoi INT,
    NoiDung TEXT,
    GhiChu TEXT,
    FileDinhKem JSONB,
    ThoiGianBatDau TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ThoiGianKetThuc TIMESTAMP,
    CONSTRAINT FK_HoSoXuLy_HoSo FOREIGN KEY (HoSoId) REFERENCES HoSo(Id) ON DELETE CASCADE,
    CONSTRAINT FK_HoSoXuLy_Users FOREIGN KEY (UserId) REFERENCES Users(Id)
);

COMMENT ON TABLE HoSoXuLy IS 'Workflow xử lý hồ sơ bởi nhân viên, full audit trail';

-- Indexes for HoSoXuLy
CREATE INDEX idx_hosoxuly_hoso ON HoSoXuLy(HoSoId);
CREATE INDEX idx_hosoxuly_user ON HoSoXuLy(UserId);
CREATE INDEX idx_hosoxuly_thoigian ON HoSoXuLy(ThoiGianBatDau DESC);
