-- ------------------------------------------------
-- Version: v1.15
-- MODIFY prefix required fields
-- -------------------------------------------------
ALTER TABLE prefix MODIFY COLUMN contact_email VARCHAR(255) NOT NULL;