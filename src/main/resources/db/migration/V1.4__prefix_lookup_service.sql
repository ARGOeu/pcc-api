-- ------------------------------------------------
-- Version: v1.4
--
-- Description: Migration that introduces the prefix lookup_service_type field which is
--  an enum that indicates which hrls service to use.
-- CENTRAL -> is for looking up the prefix in the central service
-- PRIVATE -> is for looking up the prefix in its own service
-- BOTH -> means that the service supports both central and private
-- NONE -> supports no lookup service
-- @author: Agelos Tsalapatis
-- -------------------------------------------------

ALTER TABLE prefix ADD COLUMN lookup_service_type ENUM('CENTRAL', 'PRIVATE', 'BOTH', 'NONE') NOT NULL DEFAULT 'CENTRAL';