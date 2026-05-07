-- Add firstName and lastName columns to users table if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);

-- Update existing users: extract firstName from full_name if available
UPDATE users SET first_name = SPLIT_PART(full_name, ' ', 1) WHERE first_name IS NULL;
