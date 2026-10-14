-- ------------------------------------------------
-- Version: V1.28
-- Description: Add username field to prefix accounts
-- ------------------------------------------------

ALTER TABLE account
ADD COLUMN username VARCHAR(255) NOT NULL;