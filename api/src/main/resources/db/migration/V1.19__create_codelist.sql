-- ------------------------------------------------
-- Version: v1.19
--
-- Description: Migration that introduces the codelist table with some initial values
-- -------------------------------------------------

-- enumeration table
CREATE TABLE codelist
(
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    category    ENUM('LOOKUP_SERVICE_TYPE', 'CONTRACT_TYPE') NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO codelist (id, name,category)
VALUES
       (1, 'CENTRAL','LOOKUP_SERVICE_TYPE'),
       (2, 'PRIVATE','LOOKUP_SERVICE_TYPE'),
       (3, 'BOTH','LOOKUP_SERVICE_TYPE'),
       (4, 'NONE','LOOKUP_SERVICE_TYPE'),

       (5, 'PROJECT','CONTRACT_TYPE'),
       (6, 'CONTRACT','CONTRACT_TYPE'),
       (7, 'OTHER','CONTRACT_TYPE') ;