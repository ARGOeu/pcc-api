-- ------------------------------------------------
-- Version: v1.0
--
-- Description: Migration that introduces the prefixes table in HRLS dev database
-- @author: Kostis Triantafyllakis
-- -------------------------------------------------

-- prefixes table
CREATE TABLE prefixes
(
    prefix          VARCHAR(255) NOT NULL,
    handles_count      INT          NOT NULL

);

INSERT INTO prefixes (prefix, handles_count) VALUES ('21.12132', 21);