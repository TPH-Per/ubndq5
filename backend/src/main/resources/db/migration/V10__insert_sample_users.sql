-- =============================================
-- HỆ THỐNG QUẢN LÝ QUẦY - CHỢ LỚN HCMC
-- PostgreSQL Database Migration V10
-- Insert Sample Users (Admin + NhanVien)
-- =============================================

-- =============================================
-- PASSWORD HASHING NOTE:
-- Sử dụng BCrypt để hash password
-- Password: "123456" → BCrypt hash (có thể thay đổi tùy môi trường)
-- 
-- Để tạo BCrypt hash mới, có thể dùng:
-- - Online: https://bcrypt-generator.com/
-- - Java: new BCryptPasswordEncoder().encode("123456")
-- =============================================

-- BCrypt hash cho password "123456" (cost factor = 10)
-- Hash này được generate bởi BCryptPasswordEncoder

-- =============================================
-- ADMIN ACCOUNTS
-- =============================================
INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'ADMIN001',
    'Nguyễn Văn Admin',
    'admin@cholon.gov.vn',
    '0901234567',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r WHERE r.RoleName = 'Admin'
ON CONFLICT (MaNhanVien) DO NOTHING;

INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'ADMIN002',
    'Trần Thị Quản Lý',
    'quanly@cholon.gov.vn',
    '0901234568',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r WHERE r.RoleName = 'Admin'
ON CONFLICT (MaNhanVien) DO NOTHING;

-- =============================================
-- NHANVIEN (STAFF) ACCOUNTS
-- =============================================
-- Nhân viên Quầy A (Dân số - Hộ tịch)
INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'NV001',
    'Lê Văn Nhất',
    'nv001@cholon.gov.vn',
    '0902111111',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    q.Id,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r, Quay q 
WHERE r.RoleName = 'NhanVien' AND q.MaQuay = 'Q1'
ON CONFLICT (MaNhanVien) DO NOTHING;

-- Nhân viên Quầy B (Dân số - Hộ tịch)
INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'NV002',
    'Phạm Thị Hai',
    'nv002@cholon.gov.vn',
    '0902222222',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    q.Id,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r, Quay q 
WHERE r.RoleName = 'NhanVien' AND q.MaQuay = 'Q2'
ON CONFLICT (MaNhanVien) DO NOTHING;

-- Nhân viên Quầy C (Kinh doanh)
INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'NV003',
    'Hoàng Văn Ba',
    'nv003@cholon.gov.vn',
    '0902333333',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    q.Id,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r, Quay q 
WHERE r.RoleName = 'NhanVien' AND q.MaQuay = 'Q3'
ON CONFLICT (MaNhanVien) DO NOTHING;

-- Nhân viên Quầy D (Đất đai)
INSERT INTO Users (MaNhanVien, HoTen, Email, SoDienThoai, PasswordHash, RoleId, QuayId, TrangThai, NgayTao)
SELECT 
    'NV004',
    'Đỗ Thị Tư',
    'nv004@cholon.gov.vn',
    '0902444444',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: 123456
    r.Id,
    q.Id,
    TRUE,
    CURRENT_TIMESTAMP
FROM Roles r, Quay q 
WHERE r.RoleName = 'NhanVien' AND q.MaQuay = 'Q4'
ON CONFLICT (MaNhanVien) DO NOTHING;

-- =============================================
-- VERIFICATION: Kiểm tra dữ liệu đã insert
-- =============================================
-- SELECT u.MaNhanVien, u.HoTen, u.Email, r.RoleName, q.TenQuay
-- FROM Users u
-- JOIN Roles r ON u.RoleId = r.Id
-- LEFT JOIN Quay q ON u.QuayId = q.Id
-- ORDER BY r.RoleName, u.MaNhanVien;
