
-- ------------------------------------------------
-- Version: v1.20
--
-- Description: Migration that changes the lookup_service_type colum
-- -------------------------------------------------

-- enumeration table
ALTER TABLE prefix MODIFY COLUMN  lookup_service_type INT   NOT NULL;
ALTER TABLE prefix ADD  CONSTRAINT lookup_service_type_prefix_fk FOREIGN KEY (lookup_service_type) REFERENCES codelist (id);
