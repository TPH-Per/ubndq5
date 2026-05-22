-- V31__cleanup_sample_data.sql
-- ============================================================
-- ⚠️  DESTRUCTIVE MIGRATION — DELETES ALL DATA IN THESE TABLES
-- ============================================================
-- This migration removes ALL records (not just sample data) from
-- application_history, appointment, GopYPhanAnh, application, and
-- non-admin staff. It is intended ONLY for test/dev environments
-- before E2E testing or initial production go-live (empty DB).
--
-- DO NOT run on a production database that already has real citizen
-- data — it will permanently delete all applications and appointments.
--
-- Safe targets: fresh DBs where V9-V18 seed data was the only data.
-- After running, all operational data must be entered via the UI.
-- ============================================================

-- Remove in dependency order (child tables first)
DELETE FROM application_history;
DELETE FROM appointment;
DELETE FROM "GopYPhanAnh";
DELETE FROM application;

-- Remove sample staff accounts (keep ADMIN which is re-created by DataInitializer)
DELETE FROM staff WHERE staff_code != 'ADMIN';

-- Reset queue number sequence
ALTER SEQUENCE IF EXISTS queue_number_seq RESTART WITH 1;
