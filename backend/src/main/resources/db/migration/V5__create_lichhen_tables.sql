-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V5
-- LichHen & LichSuXuLyLichHen Tables (Queue Management)
-- =============================================

-- =============================================
-- LICHHEN - Lịch hẹn của công dân
-- =============================================
CREATE TABLE LichHen (
    Id SERIAL PRIMARY KEY,
    MaLichHen VARCHAR(20) UNIQUE NOT NULL,
    CCCD VARCHAR(12) NOT NULL,
    ZaloId VARCHAR(100),
    ThuTucId INT NOT NULL,
    QuayId INT NOT NULL,
    NgayHen DATE NOT NULL,
    SoThuTu INT NOT NULL,
    PrefixSo VARCHAR(5) NOT NULL,
    ThoiGianDuKien TIME,
    ThongTinNguoiDung JSONB,
    IsScannedCCCD BOOLEAN DEFAULT FALSE,
    CCCDData JSONB,
    TrangThai INT NOT NULL DEFAULT 0,
    ThoiGianGoiSo TIMESTAMP,
    ThoiGianBatDauXuLy TIMESTAMP,
    ThoiGianKetThuc TIMESTAMP,
    NhanVienXuLyId INT,
    LyDoHuy TEXT,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LichHen_Citizens FOREIGN KEY (CCCD) REFERENCES Citizens(CCCD),
    CONSTRAINT FK_LichHen_ZaloAccounts FOREIGN KEY (ZaloId) REFERENCES ZaloAccounts(ZaloId),
    CONSTRAINT FK_LichHen_LoaiThuTuc FOREIGN KEY (ThuTucId) REFERENCES LoaiThuTuc(Id),
    CONSTRAINT FK_LichHen_Quay FOREIGN KEY (QuayId) REFERENCES Quay(Id),
    CONSTRAINT FK_LichHen_Users FOREIGN KEY (NhanVienXuLyId) REFERENCES Users(Id),
    CONSTRAINT UQ_LichHen_NgayQuaySo UNIQUE (NgayHen, QuayId, SoThuTu)
);

COMMENT ON TABLE LichHen IS 'Lịch hẹn của công dân đặt qua Zalo, queue management';
COMMENT ON COLUMN LichHen.TrangThai IS '0=Chờ gọi, 1=Đang xử lý, 2=Hoàn thành, 3=Không đến, 4=Hủy';

-- Indexes for LichHen
CREATE INDEX idx_lichhen_cccd ON LichHen(CCCD);
CREATE INDEX idx_lichhen_quay ON LichHen(QuayId);
CREATE INDEX idx_lichhen_ngayhen ON LichHen(NgayHen);
CREATE INDEX idx_lichhen_trangthai ON LichHen(TrangThai) WHERE TrangThai < 3;
CREATE INDEX idx_lichhen_queue ON LichHen(NgayHen, QuayId, SoThuTu);
CREATE INDEX idx_lichhen_nhanvien ON LichHen(NhanVienXuLyId);

-- =============================================
-- LICHSUXULYLIGHHEN - Lịch sử xử lý lịch hẹn
-- =============================================
CREATE TABLE LichSuXuLyLichHen (
    Id SERIAL PRIMARY KEY,
    LichHenId INT NOT NULL,
    UserId INT NOT NULL,
    HanhDong VARCHAR(50) NOT NULL,
    TrangThaiCu INT,
    TrangThaiMoi INT,
    LyDo TEXT,
    ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LichSuLH_LichHen FOREIGN KEY (LichHenId) REFERENCES LichHen(Id) ON DELETE CASCADE,
    CONSTRAINT FK_LichSuLH_Users FOREIGN KEY (UserId) REFERENCES Users(Id)
);

COMMENT ON TABLE LichSuXuLyLichHen IS 'Lịch sử xử lý lịch hẹn bởi nhân viên';

-- Indexes for LichSuXuLyLichHen
CREATE INDEX idx_lichsulh_lichhen ON LichSuXuLyLichHen(LichHenId);
CREATE INDEX idx_lichsulh_user ON LichSuXuLyLichHen(UserId);
CREATE INDEX idx_lichsulh_thoigian ON LichSuXuLyLichHen(ThoiGian DESC);
