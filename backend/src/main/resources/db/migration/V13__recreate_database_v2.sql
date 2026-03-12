-- =============================================
-- DATABASE V2.0 - DROP OLD TABLES AND CREATE NEW ONES
-- =============================================
-- Drop old tables (in reverse dependency order)
DROP TABLE IF EXISTS GopYPhanAnhTraLoi CASCADE;
DROP TABLE IF EXISTS GopYPhanAnh CASCADE;
DROP TABLE IF EXISTS AuditLog CASCADE;
DROP TABLE IF EXISTS HoSoXuLy CASCADE;
DROP TABLE IF EXISTS HoSo CASCADE;
DROP TABLE IF EXISTS LichSuXuLyLichHen CASCADE;
DROP TABLE IF EXISTS LichHen CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS LoaiThuTuc CASCADE;
DROP TABLE IF EXISTS Quay CASCADE;
DROP TABLE IF EXISTS ZaloAccounts CASCADE;
DROP TABLE IF EXISTS Citizens CASCADE;
DROP TABLE IF EXISTS HeThongCauHinh CASCADE;
DROP TABLE IF EXISTS ChuyenMon CASCADE;
DROP TABLE IF EXISTS Roles CASCADE;
-- Drop old views if exist
DROP VIEW IF EXISTS vw_queue_today CASCADE;
DROP VIEW IF EXISTS vw_daily_stats CASCADE;
-- =============================================
-- 1. ROLE
-- =============================================
CREATE TABLE Role (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- =============================================
-- 2. ZALO_ACCOUNT
-- Chỉ lưu zalo_id để gửi thông báo, không liên kết với công dân
-- =============================================
CREATE TABLE ZaloAccount (
    id SERIAL PRIMARY KEY,
    zalo_id VARCHAR(100) UNIQUE NOT NULL,
    zalo_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);
-- =============================================
-- 3. PROCEDURE_TYPE (Master - nhóm thủ tục theo chuyên môn)
-- =============================================
CREATE TABLE ProcedureCounter (
    id SERIAL PRIMARY KEY,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- =============================================
-- 4. PROCEDURE
-- =============================================
CREATE TABLE Procedure (
    id SERIAL PRIMARY KEY,
    procedure_code VARCHAR(20) UNIQUE NOT NULL,
    procedure_name VARCHAR(200) NOT NULL,
    description TEXT,
    processing_days INT NOT NULL DEFAULT 15,
    form_schema JSONB,
    required_documents TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    procedure_counter_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_procedure_pc FOREIGN KEY (procedure_counter_id) REFERENCES ProcedureCounter(id)
);
-- =============================================
-- 5. COUNTER (Quầy tiếp nhận)
-- =============================================
CREATE TABLE Counter (
    id SERIAL PRIMARY KEY,
    counter_code VARCHAR(10) UNIQUE NOT NULL,
    counter_name VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    procedure_counter_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_counter_pc FOREIGN KEY (procedure_counter_id) REFERENCES ProcedureCounter(id)
);
-- =============================================
-- 6. STAFF (Nhân viên)
-- =============================================
CREATE TABLE Staff (
    id SERIAL PRIMARY KEY,
    staff_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    counter_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_staff_role FOREIGN KEY (role_id) REFERENCES Role(id),
    CONSTRAINT fk_staff_counter FOREIGN KEY (counter_id) REFERENCES Counter(id)
);
CREATE INDEX idx_staff_counter ON Staff(counter_id)
WHERE is_active = TRUE;
CREATE INDEX idx_staff_role ON Staff(role_id);
-- =============================================
-- 7. APPLICATION (Hồ sơ)
-- Thông tin công dân lưu trực tiếp - không FK sang bảng Citizen riêng
-- ZaloAccount chỉ dùng để gửi thông báo
-- =============================================
CREATE TABLE Application (
    id SERIAL PRIMARY KEY,
    application_code VARCHAR(20) UNIQUE NOT NULL,
    procedure_id INT NOT NULL,
    -- Thông tin công dân (inline - không cần bảng Citizen riêng)
    citizen_cccd VARCHAR(12) NOT NULL,
    citizen_name VARCHAR(100) NOT NULL,
    citizen_phone VARCHAR(15),
    citizen_email VARCHAR(100),
    -- Zalo (chỉ lưu để gửi thông báo)
    zalo_account_id INT,
    current_phase INT NOT NULL DEFAULT 2,
    -- Bắt đầu ở PENDING
    queue_number INT,
    queue_prefix VARCHAR(5),
    deadline DATE,
    priority INT DEFAULT 0,
    cancel_reason TEXT,
    cancel_type INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_app_procedure FOREIGN KEY (procedure_id) REFERENCES Procedure(id),
    CONSTRAINT fk_app_zalo FOREIGN KEY (zalo_account_id) REFERENCES ZaloAccount(id)
);
CREATE INDEX idx_app_cccd ON Application(citizen_cccd);
CREATE INDEX idx_app_procedure ON Application(procedure_id);
CREATE INDEX idx_app_phase ON Application(current_phase)
WHERE current_phase < 4;
-- =============================================
-- 8. APPLICATION_HISTORY (Lịch sử xử lý hồ sơ)
-- =============================================
CREATE TABLE ApplicationHistory (
    id SERIAL PRIMARY KEY,
    application_id INT NOT NULL,
    counter_id INT,
    -- Nullable: cho phép ghi từ Mini App (không có quầy)
    staff_id INT,
    phase_from INT,
    phase_to INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    content TEXT,
    -- Thông tin lịch hẹn (lưu theo từng lần)
    appointment_date DATE,
    expected_time TIME,
    queue_number INT,
    queue_prefix VARCHAR(5),
    form_data JSONB,
    attachments JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_app FOREIGN KEY (application_id) REFERENCES Application(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_counter FOREIGN KEY (counter_id) REFERENCES Counter(id),
    CONSTRAINT fk_history_staff FOREIGN KEY (staff_id) REFERENCES Staff(id)
);
CREATE INDEX idx_history_app ON ApplicationHistory(application_id);
CREATE INDEX idx_history_appointment ON ApplicationHistory(appointment_date);
CREATE INDEX idx_history_time ON ApplicationHistory(created_at DESC);
-- =============================================
-- 9. APPOINTMENT (Lịch hẹn cụ thể - slot đặt chỗ)
-- =============================================
CREATE TABLE Appointment (
    id SERIAL PRIMARY KEY,
    application_id INT NOT NULL,
    staff_id INT,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status INT NOT NULL DEFAULT 0,
    -- 0:SCHEDULED 1:COMPLETED 2:CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_app FOREIGN KEY (application_id) REFERENCES Application(id),
    CONSTRAINT fk_appointment_staff FOREIGN KEY (staff_id) REFERENCES Staff(id)
);
CREATE INDEX idx_appointment_date ON Appointment(appointment_date);
CREATE INDEX idx_appointment_status ON Appointment(status);
-- =============================================
-- 10. REPORT (Phản ánh - dùng bởi Staff)
-- Thông tin công dân lưu inline, application là optional
-- =============================================
CREATE TABLE Report (
    id SERIAL PRIMARY KEY,
    -- Thông tin công dân (inline)
    citizen_cccd VARCHAR(12) NOT NULL,
    citizen_name VARCHAR(100),
    citizen_phone VARCHAR(15),
    -- Liên kết hồ sơ (optional)
    application_id INT,
    report_type INT NOT NULL DEFAULT 0,
    -- 0:Góp ý, 1:Khiếu nại, 2:Khen ngợi
    title VARCHAR(200),
    content TEXT NOT NULL,
    attachments JSONB,
    status INT DEFAULT 0,
    -- 0:Mới, 1:Đang xử lý, 2:Đã giải quyết
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_app FOREIGN KEY (application_id) REFERENCES Application(id)
);
CREATE INDEX idx_report_cccd ON Report(citizen_cccd);
CREATE INDEX idx_report_app ON Report(application_id);
CREATE INDEX idx_report_status ON Report(status)
WHERE status < 2;
-- =============================================
-- 11. REPLY (Phản hồi của nhân viên)
-- =============================================
CREATE TABLE Reply (
    id SERIAL PRIMARY KEY,
    report_id INT NOT NULL,
    staff_id INT NOT NULL,
    content TEXT NOT NULL,
    attachments JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reply_report FOREIGN KEY (report_id) REFERENCES Report(id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_staff FOREIGN KEY (staff_id) REFERENCES Staff(id)
);
CREATE INDEX idx_reply_report ON Reply(report_id);
-- =============================================
-- VIEWS
-- =============================================
CREATE VIEW vw_queue_today AS
SELECT a.id,
    a.application_code,
    COALESCE(a.queue_prefix, '') || COALESCE(a.queue_number::TEXT, '') AS queue_display,
    a.queue_number,
    a.citizen_name,
    a.citizen_phone,
    p.procedure_name,
    a.current_phase,
    a.created_at
FROM Application a
    JOIN Procedure p ON a.procedure_id = p.id
WHERE a.current_phase IN (1, 3)
ORDER BY a.queue_number;
CREATE VIEW vw_daily_stats AS
SELECT h.counter_id,
    co.counter_name,
    DATE(h.created_at) AS date,
    COUNT(*) FILTER (
        WHERE h.phase_to = 1
    ) AS queue_count,
    COUNT(*) FILTER (
        WHERE h.phase_to = 3
    ) AS processing_count,
    COUNT(*) FILTER (
        WHERE h.phase_to = 4
    ) AS completed_count,
    COUNT(*) FILTER (
        WHERE h.phase_to = 0
    ) AS cancelled_count,
    COUNT(*) FILTER (
        WHERE h.phase_to = 2
    ) AS pending_count
FROM ApplicationHistory h
    JOIN Counter co ON h.counter_id = co.id
GROUP BY h.counter_id,
    co.counter_name,
    DATE(h.created_at);
-- =============================================
-- INITIAL DATA
-- =============================================
INSERT INTO Role (role_name, display_name, description)
VALUES ('Admin', 'Quản trị viên', 'Full system access'),
    ('Staff', 'Nhân viên', 'Counter staff');
-- ProcedureCounter (procedure types)
INSERT INTO ProcedureCounter (is_active)
VALUES (TRUE),
    (TRUE),
    (TRUE);
-- Counters
INSERT INTO Counter (
        counter_code,
        counter_name,
        location,
        procedure_counter_id
    )
VALUES ('Q01', 'Quầy 1 - Hộ tịch', 'Tầng 1', 1),
    ('Q02', 'Quầy 2 - Đất đai', 'Tầng 1', 2),
    ('Q03', 'Quầy 3 - Kinh doanh', 'Tầng 2', 3);
-- Procedures
INSERT INTO Procedure (
        procedure_code,
        procedure_name,
        processing_days,
        procedure_counter_id
    )
VALUES ('TT001', 'Đăng ký khai sinh', 3, 1),
    ('TT002', 'Đăng ký kết hôn', 5, 1),
    (
        'TT003',
        'Cấp giấy xác nhận tình trạng hôn nhân',
        3,
        1
    ),
    ('TT004', 'Đăng ký biến động đất đai', 15, 2),
    ('TT005', 'Cấp giấy phép kinh doanh', 10, 3);
-- Dữ liệu này sẽ được khởi tạo bởi DataInitializer trong backend
-- (Đảm bảo password được hash bằng BCrypt chuẩn từ Java)
-- Sample applications (citizen info inline)
INSERT INTO Application (
        application_code,
        procedure_id,
        citizen_cccd,
        citizen_name,
        citizen_phone,
        citizen_email,
        current_phase,
        queue_number,
        queue_prefix,
        deadline,
        priority
    )
VALUES (
        'HS-20260101-001',
        1,
        '079001234567',
        'Phạm Văn X',
        '0901234567',
        'pvx@gmail.com',
        2,
        1,
        'TT',
        CURRENT_DATE + 3,
        0
    ),
    (
        'HS-20260101-002',
        2,
        '079001234568',
        'Hoàng Thị Y',
        '0901234568',
        'hty@gmail.com',
        2,
        2,
        'TT',
        CURRENT_DATE + 5,
        0
    );