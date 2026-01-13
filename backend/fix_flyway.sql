-- =============================================
-- FIX FLYWAY SCHEMA HISTORY
-- Chạy script này trong pgAdmin/DBeaver
-- =============================================

-- Xóa history cũ và thêm records cho V1-V9
DELETE FROM flyway_schema_history WHERE installed_rank > 0;

-- Thêm baseline cho tất cả migrations
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES 
(1, '1', 'create foundation tables', 'SQL', 'V1__create_foundation_tables.sql', NULL, 'postgres', NOW(), 1, true),
(2, '2', 'create citizens zalo tables', 'SQL', 'V2__create_citizens_zalo_tables.sql', NULL, 'postgres', NOW(), 1, true),
(3, '3', 'create quay loaithutuc tables', 'SQL', 'V3__create_quay_loaithutuc_tables.sql', NULL, 'postgres', NOW(), 1, true),
(4, '4', 'create users table', 'SQL', 'V4__create_users_table.sql', NULL, 'postgres', NOW(), 1, true),
(5, '5', 'create lichhen tables', 'SQL', 'V5__create_lichhen_tables.sql', NULL, 'postgres', NOW(), 1, true),
(6, '6', 'create hoso tables', 'SQL', 'V6__create_hoso_tables.sql', NULL, 'postgres', NOW(), 1, true),
(7, '7', 'create gopy tables', 'SQL', 'V7__create_gopy_tables.sql', NULL, 'postgres', NOW(), 1, true),
(8, '8', 'create auditlog table', 'SQL', 'V8__create_auditlog_table.sql', NULL, 'postgres', NOW(), 1, true),
(9, '9', 'insert initial data', 'SQL', 'V9__insert_initial_data.sql', NULL, 'postgres', NOW(), 1, true);

-- Kiểm tra kết quả
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
