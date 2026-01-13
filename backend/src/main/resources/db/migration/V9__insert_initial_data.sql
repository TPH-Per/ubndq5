-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V9
-- Insert Initial Data (Seed Data)
-- =============================================

-- =============================================
-- ROLES: CHỈ Admin và NhanVien
-- =============================================
INSERT INTO Roles (RoleName, DisplayName, MoTa) VALUES 
    ('Admin', 'Quản trị viên', 'Quản lý hệ thống, phân công nhân viên, phê duyệt hồ sơ'),
    ('NhanVien', 'Nhân viên', 'Xử lý lịch hẹn và hồ sơ tại quầy được gán');

-- =============================================
-- CHUYENMON: Các chuyên môn nghiệp vụ
-- =============================================
INSERT INTO ChuyenMon (MaChuyenMon, TenChuyenMon, MoTa) VALUES 
    ('CM_DS', 'Dân số - Hộ tịch', 'Xử lý các thủ tục về hộ tịch, khai sinh, hộ khẩu, CCCD'),
    ('CM_KD', 'Kinh doanh', 'Xử lý các thủ tục về giấy phép kinh doanh, đăng ký doanh nghiệp'),
    ('CM_DD', 'Đất đai', 'Xử lý các thủ tục về quyền sử dụng đất, chuyển nhượng đất'),
    ('CM_XD', 'Xây dựng', 'Xử lý giấy phép xây dựng, cấp phép quy hoạch');

-- =============================================
-- QUAY: Các quầy giao dịch
-- =============================================
INSERT INTO Quay (MaQuay, TenQuay, ViTri, PrefixSo, ChuyenMonId) VALUES 
    ('Q1', 'Quầy A', 'Tầng 1 - Khu A', 'A', 1),
    ('Q2', 'Quầy B', 'Tầng 1 - Khu A', 'B', 1),
    ('Q3', 'Quầy C', 'Tầng 1 - Khu B', 'C', 2),
    ('Q4', 'Quầy D', 'Tầng 1 - Khu B', 'D', 3);

-- =============================================
-- LOAITHUTUC: Các loại thủ tục hành chính
-- =============================================
INSERT INTO LoaiThuTuc (MaThuTuc, TenThuTuc, ChuyenMonId, ThoiGianXuLy, MoTa) VALUES 
    ('KS', 'Đăng ký khai sinh', 1, 20, 'Đăng ký khai sinh cho trẻ em mới sinh'),
    ('HK', 'Xác nhận hộ khẩu', 1, 15, 'Xác nhận thay đổi hộ khẩu, đăng ký tạm trú'),
    ('CCCD', 'Cấp/Đổi CCCD', 1, 25, 'Cấp mới hoặc đổi CCCD'),
    ('GPKD', 'Giấy phép kinh doanh', 2, 30, 'Đăng ký giấy phép kinh doanh hộ cá thể'),
    ('DKDN', 'Đăng ký doanh nghiệp', 2, 45, 'Đăng ký thành lập doanh nghiệp'),
    ('DD_CN', 'Chuyển nhượng đất', 3, 60, 'Xử lý hồ sơ chuyển nhượng quyền sử dụng đất');

-- =============================================
-- HETHONGCAUHINH: Cấu hình hệ thống
-- =============================================
INSERT INTO HeThongCauHinh (ConfigKey, ConfigValue, MoTa) VALUES 
    ('GIO_BAT_DAU', '08:00', 'Giờ bắt đầu làm việc'),
    ('GIO_KET_THUC', '17:00', 'Giờ kết thúc làm việc'),
    ('GIO_NGHI_TRUA', '11:30', 'Giờ bắt đầu nghỉ trưa'),
    ('GIO_QUAY_LAI', '13:00', 'Giờ làm việc lại sau nghỉ trưa'),
    ('SO_LUONG_TOI_DA_NGAY', '100', 'Số lượng lịch hẹn tối đa mỗi ngày'),
    ('THOI_GIAN_BUFFER', '5', 'Thời gian buffer giữa các lịch hẹn (phút)');
