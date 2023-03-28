-- ------------------------------------------------
-- Version: v1.9
--
-- Description: Update lookup_service_type default
-- -------------------------------------------------

ALTER TABLE prefix MODIFY lookup_service_type ENUM('CENTRAL', 'PRIVATE', 'BOTH', 'NONE') NOT NULL DEFAULT 'NONE';