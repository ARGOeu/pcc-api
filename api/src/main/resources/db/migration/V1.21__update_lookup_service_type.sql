
-- ------------------------------------------------
-- Version: v1.21
--
-- Description: Migration that changes the contract_type colum
-- -------------------------------------------------

-- enumeration table
ALTER TABLE prefix ADD COLUMN  lookup_service_type_id INT NOT NULL;
ALTER TABLE prefix ADD  CONSTRAINT lookup_service_type_prefix_fk FOREIGN KEY (lookup_service_type_id) REFERENCES codelist (id);
ALTER TABLE prefix ADD CONSTRAINT  CHK_LOOKUP_SERVICE_TYPE CHECK (lookup_service_type_id IN (1,2,3,4));
