-- ------------------------------------------------
-- Version: v1.11
-- MODIFY contract_end column
-- -------------------------------------------------


ALTER TABLE prefix MODIFY COLUMN contract_end TIMESTAMP NULL DEFAULT NULL;
