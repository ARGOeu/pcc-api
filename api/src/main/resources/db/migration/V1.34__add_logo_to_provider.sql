-- ------------------------------------------------
-- Version: v1.34
--
-- Description: Adds logo field to provider table
-- ------------------------------------------------

ALTER TABLE provider
    ADD COLUMN logo VARCHAR(2048) NULL;
