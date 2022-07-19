-- ------------------------------------------------
-- Version: v1.1
--
-- Description: Migration that introduces the domain table
-- @author: Agelos Tsalapatis
-- -------------------------------------------------

-- domain table
CREATE TABLE domain
(
    id          INT          NOT NULL AUTO_INCREMENT,
    domain_id   VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id)
);
