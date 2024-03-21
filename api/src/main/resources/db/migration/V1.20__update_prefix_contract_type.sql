
-- ------------------------------------------------
-- Version: v1.20
--
-- Description: Migration that changes the contract_type colum
-- -------------------------------------------------

-- enumeration table
ALTER TABLE prefix ADD COLUMN  contract_type_id INT NOT NULL;
ALTER TABLE prefix ADD  CONSTRAINT contract_type_prefix_fk FOREIGN KEY (contract_type_id) REFERENCES codelist (id) ;
ALTER TABLE prefix ADD CONSTRAINT CHK_CONTRACT_TYPE CHECK (contract_type_id IN (5,6,7));


