-- V26: Xóa cột citizen_cccd khỏi appointment
-- Thông tin công dân (CCCD) chỉ lưu trên bảng application, không cần duplicate ở appointment
ALTER TABLE appointment DROP COLUMN IF EXISTS citizen_cccd;
