-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V7
-- GopYPhanAnh & GopYPhanAnhTraLoi Tables (Feedback System)
-- =============================================

-- =============================================
-- GOPYPHANANH - Góp ý, khiếu nại, khen ngợi
-- =============================================
CREATE TABLE GopYPhanAnh (
    Id SERIAL PRIMARY KEY,
    MaGopY VARCHAR(20) UNIQUE NOT NULL,
    CCCD VARCHAR(12) NOT NULL,
    ZaloId VARCHAR(100),
    LoaiGopY INT NOT NULL,
    TieuDe VARCHAR(200) NOT NULL,
    NoiDung TEXT NOT NULL,
    FileDinhKem JSONB,
    HoSoId INT,
    QuayId INT,
    NhanVienLienQuanId INT,
    TrangThai INT NOT NULL DEFAULT 0,
    DoUuTien INT DEFAULT 0,
    NhanVienXuLyId INT,
    GhiChuNoiBo TEXT,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayXuLy TIMESTAMP,
    NgayHoanThanh TIMESTAMP,
    DanhGia INT,
    PhanHoiDanhGia TEXT,
    CONSTRAINT FK_GopY_Citizens FOREIGN KEY (CCCD) REFERENCES Citizens(CCCD),
    CONSTRAINT FK_GopY_ZaloAccounts FOREIGN KEY (ZaloId) REFERENCES ZaloAccounts(ZaloId),
    CONSTRAINT FK_GopY_HoSo FOREIGN KEY (HoSoId) REFERENCES HoSo(Id),
    CONSTRAINT FK_GopY_Quay FOREIGN KEY (QuayId) REFERENCES Quay(Id),
    CONSTRAINT FK_GopY_NhanVienLienQuan FOREIGN KEY (NhanVienLienQuanId) REFERENCES Users(Id),
    CONSTRAINT FK_GopY_NhanVienXuLy FOREIGN KEY (NhanVienXuLyId) REFERENCES Users(Id)
);

COMMENT ON TABLE GopYPhanAnh IS 'Góp ý, khiếu nại, khen ngợi từ công dân';
COMMENT ON COLUMN GopYPhanAnh.LoaiGopY IS '1=Góp ý, 2=Khiếu nại, 3=Khen ngợi';

-- Indexes for GopYPhanAnh
CREATE INDEX idx_gopy_cccd ON GopYPhanAnh(CCCD);
CREATE INDEX idx_gopy_loai ON GopYPhanAnh(LoaiGopY);
CREATE INDEX idx_gopy_trangthai ON GopYPhanAnh(TrangThai) WHERE TrangThai < 3;
CREATE INDEX idx_gopy_ngaytao ON GopYPhanAnh(NgayTao DESC);
CREATE INDEX idx_gopy_hoso ON GopYPhanAnh(HoSoId) WHERE HoSoId IS NOT NULL;
CREATE INDEX idx_gopy_quay ON GopYPhanAnh(QuayId) WHERE QuayId IS NOT NULL;
CREATE INDEX idx_gopy_nhanvienlienquan ON GopYPhanAnh(NhanVienLienQuanId) WHERE NhanVienLienQuanId IS NOT NULL;
CREATE INDEX idx_gopy_nhanvienxuly ON GopYPhanAnh(NhanVienXuLyId) WHERE NhanVienXuLyId IS NOT NULL;

-- =============================================
-- GOPYPHANANHTRALOI - Phản hồi góp ý
-- =============================================
CREATE TABLE GopYPhanAnhTraLoi (
    Id SERIAL PRIMARY KEY,
    GopYId INT NOT NULL,
    UserId INT NOT NULL,
    NoiDung TEXT NOT NULL,
    FileDinhKem JSONB,
    TrangThaiCu INT,
    TrangThaiMoi INT,
    IsPublic BOOLEAN DEFAULT TRUE,
    NgayTraLoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_GopYTraLoi_GopY FOREIGN KEY (GopYId) REFERENCES GopYPhanAnh(Id) ON DELETE CASCADE,
    CONSTRAINT FK_GopYTraLoi_Users FOREIGN KEY (UserId) REFERENCES Users(Id)
);

COMMENT ON TABLE GopYPhanAnhTraLoi IS 'Phản hồi của nhân viên cho góp ý của công dân';

-- Indexes for GopYPhanAnhTraLoi
CREATE INDEX idx_gopytralloi_gopy ON GopYPhanAnhTraLoi(GopYId);
CREATE INDEX idx_gopytralloi_user ON GopYPhanAnhTraLoi(UserId);
CREATE INDEX idx_gopytralloi_ngay ON GopYPhanAnhTraLoi(NgayTraLoi DESC);
