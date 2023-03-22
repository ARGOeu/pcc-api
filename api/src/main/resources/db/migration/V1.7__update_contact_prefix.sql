-- ------------------------------------------------
-- Version: v1.7
--
-- Description: Add columns contact_name, contact_email to prefix
-- -------------------------------------------------

ALTER TABLE prefix
ADD COLUMN contact_name VARCHAR(255),
ADD COLUMN contact_email VARCHAR(255);

