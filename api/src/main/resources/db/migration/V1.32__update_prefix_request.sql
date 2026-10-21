-- ------------------------------------------------
-- Version: v1.32
--
-- Description: Adds created_by to prefix_request
-- ------------------------------------------------

ALTER TABLE prefix_request
    ADD COLUMN created_by VARCHAR(255) NULL;
ALTER TABLE prefix_request
    ADD COLUMN handled_by VARCHAR(255) NULL;