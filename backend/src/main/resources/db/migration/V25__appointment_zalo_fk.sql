-- V25: Appointment gắn với ZaloAccount (1 zalo = nhiều appointment)
-- citizenCccd chỉ lưu trên Application, Appointment không cần giữ

-- Thêm cột nullable trước để không fail trên existing data
ALTER TABLE appointment ADD COLUMN IF NOT EXISTS zalo_account_id INTEGER;

-- FK constraint
ALTER TABLE appointment
    ADD CONSTRAINT fk_appointment_zalo
    FOREIGN KEY (zalo_account_id) REFERENCES zaloaccount(id);

-- Backfill từ application (dữ liệu cũ)
UPDATE appointment a
SET zalo_account_id = (SELECT ap.zalo_account_id FROM application ap WHERE ap.id = a.application_id)
WHERE a.application_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_appointment_zalo_account ON appointment(zalo_account_id);
