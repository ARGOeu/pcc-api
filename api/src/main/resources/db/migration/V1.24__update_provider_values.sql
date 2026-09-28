-- ------------------------------------------------
-- Version: v1.24
--
-- Description: Migration that updates the available providers.
-- -------------------------------------------------

UPDATE provider
SET name = 'Surf'
WHERE id = 3;

DELETE FROM provider
WHERE id = 2;