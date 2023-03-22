-- ------------------------------------------------
-- Version: v1.6
--
-- Description: Add column resolvable (boolean) to prefixes
-- -------------------------------------------------

ALTER TABLE prefix
ADD resolvable  BOOLEAN DEFAULT TRUE;