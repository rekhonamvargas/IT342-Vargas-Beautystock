-- +goose Up
-- Increase column sizes for users table to handle longer values
ALTER TABLE users
  ALTER COLUMN email TYPE VARCHAR(500),
  ALTER COLUMN full_name TYPE VARCHAR(500),
  ALTER COLUMN password_hash TYPE VARCHAR(500),
  ALTER COLUMN profile_image_url TYPE TEXT,
  ALTER COLUMN notification_email TYPE VARCHAR(500);

-- +goose Down
-- Revert column sizes for users table
ALTER TABLE users
  ALTER COLUMN email TYPE VARCHAR(255),
  ALTER COLUMN full_name TYPE VARCHAR(255),
  ALTER COLUMN password_hash TYPE VARCHAR(255),
  ALTER COLUMN profile_image_url TYPE VARCHAR(255),
  ALTER COLUMN notification_email TYPE VARCHAR(255);
