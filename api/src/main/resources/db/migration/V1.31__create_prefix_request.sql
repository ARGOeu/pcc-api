-- ------------------------------------------------
-- Version: v1.31
--
-- Description: Creates the prefix_request table
-- ------------------------------------------------

CREATE TABLE prefix_request
(
    id                      INT          NOT NULL AUTO_INCREMENT,
    contact_person_name     VARCHAR(255) NOT NULL,
    contact_person_surname  VARCHAR(255) NOT NULL,
    contact_person_email    VARCHAR(255) NOT NULL,
    contact_person_phone    VARCHAR(50),
    country                 VARCHAR(100),
    organization_name       VARCHAR(255) NOT NULL,
    project_name            VARCHAR(255),
    project_url             VARCHAR(2048),
    project_description     TEXT,
    expected_handles        VARCHAR(50),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    status_reason           TEXT,
    provider_id             INT,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP   NULL DEFAULT NULL
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_prefix_request_provider
        FOREIGN KEY (provider_id)
            REFERENCES provider (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_prefix_request_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),

    CONSTRAINT chk_prefix_request_expected_handles
        CHECK (
            expected_handles IS NULL
                OR expected_handles IN (
                                        'UP_TO_1000',
                                        'FROM_1000_TO_10000',
                                        'FROM_10000_TO_500000',
                                        'OVER_1000000'
                )
            ),

    INDEX idx_prefix_request_status (status),
    INDEX idx_prefix_request_provider (provider_id),
    INDEX idx_prefix_request_email (contact_person_email),
    INDEX idx_prefix_request_created_at (created_at)
);