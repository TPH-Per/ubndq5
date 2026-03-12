-- Issue #5: Daily queue number sequence to prevent race conditions on concurrent requests.
-- Application code resets this sequence at midnight via ApplicationSchedulerService.

CREATE SEQUENCE IF NOT EXISTS queue_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

-- Seed sequence from current max to avoid collisions with existing data
SELECT setval('queue_number_seq',
    COALESCE((SELECT MAX(queue_number) FROM application), 0) + 1,
    false
);
