-- V6__add_notification_columns.sql
-- Add notification email and notification preference columns to users table

ALTER TABLE users ADD COLUMN notification_email VARCHAR(255);
ALTER TABLE users ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT false;
