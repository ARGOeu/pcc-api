-- ------------------------------------------------
-- Version: V1.29
-- Description: Create contact and provider_contact tables
-- ------------------------------------------------

CREATE TABLE contact
(
    id            INT          NOT NULL AUTO_INCREMENT,
    contact_name  VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_type  SMALLINT     NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uq_contact_name_email_type
        UNIQUE (contact_name, contact_email, contact_type)
);

CREATE TABLE provider_contact
(
    provider_id INT NOT NULL,
    contact_id  INT NOT NULL,

    PRIMARY KEY (provider_id, contact_id),

    CONSTRAINT fk_provider_contact_provider
        FOREIGN KEY (provider_id)
            REFERENCES provider (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_provider_contact_contact
        FOREIGN KEY (contact_id)
            REFERENCES contact (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT
);