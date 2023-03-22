-- ------------------------------------------------
-- Version: v1.10
-- Add contract_type column
-- -------------------------------------------------


ALTER TABLE prefix
ADD COLUMN contract_type ENUM('PROJECT', 'CONTRACT', 'OTHER');
