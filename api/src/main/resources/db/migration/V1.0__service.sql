-- ------------------------------------------------
-- Version: v1.0
--
-- Description: Migration that introduces the service table with some initial values
-- @author: Agelos Tsalapatis
-- -------------------------------------------------

-- service table
CREATE TABLE service
(
    id   INT          NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- initial values for the service entity
INSERT INTO service (id, name)
VALUES (1, 'B2HANDLE'),
       (2, 'B2SAFE'),
       (3, 'B2ACCESS');