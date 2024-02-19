-- ------------------------------------------------
-- Version: v1.14
-- MODIFY prefix required fields
-- -------------------------------------------------
ALTER TABLE prefix MODIFY COLUMN contact_name VARCHAR(255) NOT NULL;