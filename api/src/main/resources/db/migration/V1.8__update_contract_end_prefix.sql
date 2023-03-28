-- ------------------------------------------------
-- Version: v1.8
--
-- Description: Add column contract_end to prefix
-- -------------------------------------------------

ALTER TABLE prefix
ADD COLUMN contract_end TIMESTAMP;

