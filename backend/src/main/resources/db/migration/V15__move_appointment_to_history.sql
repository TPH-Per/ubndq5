-- V15: Rename bảng và thêm cột appointment vào application_history
-- Lý do: Tên bảng dùng snake_case cho tường minh, và lưu lịch sử các lần hẹn
-- NOTE: V13 đã tạo application_history với cột appointment_date, expected_time rồi
--       V15 chỉ xử lý phần rename nếu còn bảng cũ và recreate views

-- 1. Drop views trước (vì có thể phụ thuộc vào cột cũ)
DROP VIEW IF EXISTS vw_queue_today CASCADE;
DROP VIEW IF EXISTS vw_daily_stats CASCADE;

-- 2. Rename bảng từ applicationhistory sang application_history (nếu tên cũ còn tồn tại)
DO $$ BEGIN
    IF EXISTS (
        SELECT FROM information_schema.tables WHERE table_name = 'applicationhistory'
    ) THEN
        ALTER TABLE applicationhistory RENAME TO application_history;
    END IF;
END $$;

-- 3. Thêm cột appointment vào application_history nếu chưa có
ALTER TABLE application_history
    ADD COLUMN IF NOT EXISTS appointment_date DATE,
    ADD COLUMN IF NOT EXISTS expected_time TIME;

-- 4. Thêm index cho appointment_date
CREATE INDEX IF NOT EXISTS idx_history_appointment ON application_history(appointment_date);

-- 5. Recreate views (citizen info từ application.citizen_name, citizen_phone)
CREATE OR REPLACE VIEW vw_queue_today AS
SELECT
    a.id,
    a.application_code,
    COALESCE(a.queue_prefix, '') || COALESCE(a.queue_number::TEXT, '') AS queue_display,
    a.queue_number,
    a.citizen_name,
    a.citizen_phone,
    p.procedure_name,
    h.expected_time,
    a.current_phase,
    a.created_at
FROM application a
    JOIN procedure p ON a.procedure_id = p.id
    LEFT JOIN LATERAL (
        SELECT expected_time
        FROM application_history
        WHERE application_id = a.id
            AND expected_time IS NOT NULL
        ORDER BY created_at DESC
        LIMIT 1
    ) h ON TRUE
WHERE a.current_phase IN (1, 3)
ORDER BY a.queue_number;

CREATE OR REPLACE VIEW vw_daily_stats AS
SELECT
    h.counter_id,
    co.counter_name,
    DATE(h.created_at) AS date,
    COUNT(*) FILTER (WHERE h.phase_to = 1) AS queue_count,
    COUNT(*) FILTER (WHERE h.phase_to = 3) AS processing_count,
    COUNT(*) FILTER (WHERE h.phase_to = 4) AS completed_count,
    COUNT(*) FILTER (WHERE h.phase_to = 0) AS cancelled_count,
    COUNT(*) FILTER (WHERE h.phase_to = 2) AS pending_count
FROM application_history h
    JOIN counter co ON h.counter_id = co.id
GROUP BY h.counter_id, co.counter_name, DATE(h.created_at);

COMMENT ON COLUMN application_history.appointment_date IS 'Ngày hẹn cho lần này';
COMMENT ON COLUMN application_history.expected_time IS 'Giờ dự kiến cho lần hẹn này';