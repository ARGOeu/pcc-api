-- ------------------------------------------------
-- Version: v1.18
-- MODIFY prefix required fields
-- -------------------------------------------------
ALTER TABLE prefix MODIFY COLUMN lookup_service_type ENUM('CENTRAL', 'PRIVATE', 'BOTH', 'NONE') NOT NULL;