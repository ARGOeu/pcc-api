-- ------------------------------------------------
-- Version: v1.3
--
-- Description: Migration that introduces the prefix table
-- @author: Agelos Tsalapatis
-- -------------------------------------------------

-- prefix table
CREATE TABLE prefix
(
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    owner       VARCHAR(255) NOT NULL,
    used_by     VARCHAR(255),
    status      INT          NOT NULL,
    provider_id INT          NOT NULL,
    service_id  INT          NOT NULL,
    domain_id   INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT provider_prefix_fk FOREIGN KEY (provider_id) REFERENCES provider (id),
    CONSTRAINT service_prefix_fk FOREIGN KEY (service_id) REFERENCES service (id),
    CONSTRAINT domain_prefix_fk FOREIGN KEY (domain_id) REFERENCES domain (id)
);