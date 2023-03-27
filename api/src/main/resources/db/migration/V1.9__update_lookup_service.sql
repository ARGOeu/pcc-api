-- ------------------------------------------------
-- Version: v1.9
--
-- Description: Update lookup_service_type to be NULLABLE
-- -------------------------------------------------

ALTER TABLE prefix MODIFY lookup_service_type ENUM('CENTRAL', 'PRIVATE', 'BOTH', 'NONE') DEFAULT 'CENTRAL';