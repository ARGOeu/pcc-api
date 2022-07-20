-- ------------------------------------------------
-- Version: v1.2
--
-- Description: Migration that introduces the provider table with some initial values
-- @author: Agelos Tsalapatis
-- -------------------------------------------------

-- provider table
CREATE TABLE provider
(
    id   INT          NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO provider (id, name)
VALUES (1, 'GRNET'),
       (2, 'DKRZ'),
       (3, 'SURF');