-- Change user_id column to accept UUID type
ALTER TABLE favorites ALTER COLUMN user_id TYPE uuid USING user_id::text::uuid;
