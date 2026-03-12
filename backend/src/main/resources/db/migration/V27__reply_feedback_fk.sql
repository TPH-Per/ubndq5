-- V27: Connect Reply to Feedback (GopYPhanAnh)
-- reply.report_id: make nullable (existing replies keep their data)
-- reply.feedback_id: new FK to GopYPhanAnh for citizen feedback replies
ALTER TABLE reply ALTER COLUMN report_id DROP NOT NULL;
ALTER TABLE reply ADD COLUMN IF NOT EXISTS feedback_id INT REFERENCES "GopYPhanAnh"(id) ON DELETE CASCADE;
