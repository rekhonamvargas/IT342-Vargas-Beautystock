-- Fix product table columns to support larger data
ALTER TABLE products ALTER COLUMN image_url TYPE TEXT;
