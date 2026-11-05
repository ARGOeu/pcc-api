-- ------------------------------------------------
-- Version: v1.36
--
-- Description: Adds epic healthcheck field to provider table
-- ------------------------------------------------

ALTER TABLE prefix
ADD COLUMN epic_healthcheck BOOLEAN DEFAULT NULL;