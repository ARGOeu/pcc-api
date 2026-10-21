-- ------------------------------------------------
-- Version: v1.30
--
-- Description: Adds an optional description to providers
-- ------------------------------------------------

ALTER TABLE provider
    ADD COLUMN description VARCHAR(255) NULL;