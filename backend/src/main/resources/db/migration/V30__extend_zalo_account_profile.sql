ALTER TABLE zaloaccount
    ADD COLUMN IF NOT EXISTS zalo_avatar VARCHAR(500),
    ADD COLUMN IF NOT EXISTS oa_user_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_zaloaccount_oa_user_id
    ON zaloaccount(oa_user_id)
    WHERE oa_user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_zaloaccount_phone_number
    ON zaloaccount(phone_number)
    WHERE phone_number IS NOT NULL;
