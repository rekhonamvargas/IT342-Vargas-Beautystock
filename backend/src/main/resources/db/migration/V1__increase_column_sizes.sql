-- Increase column sizes for products table
-- V1__increase_column_sizes.sql

ALTER TABLE products
  ALTER COLUMN owner_email TYPE VARCHAR(500),
  ALTER COLUMN name TYPE VARCHAR(500),
  ALTER COLUMN brand TYPE VARCHAR(500),
  ALTER COLUMN category TYPE VARCHAR(500),
  ALTER COLUMN purchase_location TYPE VARCHAR(500),
  ALTER COLUMN status TYPE VARCHAR(500),
  ALTER COLUMN description TYPE VARCHAR(5000),
  ALTER COLUMN image_url TYPE TEXT;
