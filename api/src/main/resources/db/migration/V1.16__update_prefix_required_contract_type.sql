-- ------------------------------------------------
-- Version: v1.16
-- MODIFY prefix required fields
-- -------------------------------------------------
ALTER TABLE prefix MODIFY COLUMN contract_type ENUM('PROJECT', 'CONTRACT', 'OTHER') NOT NULL;