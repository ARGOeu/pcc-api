-- ------------------------------------------------
-- Version: V1.26
-- Description: Create prefix_invitation table for prefix accounts invitations
-- ------------------------------------------------

CREATE TABLE prefix_invitation (
    id VARCHAR(36) NOT NULL,
    prefix_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    created_by VARCHAR(255),
    responded_by VARCHAR(255),

    PRIMARY KEY (id),

    CONSTRAINT fk_provider_invitation_provider
        FOREIGN KEY (prefix_id)
        REFERENCES prefix(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_prefix_invitation_provider
    ON prefix_invitation(prefix_id);

CREATE INDEX idx_provider_invitation_lookup
    ON prefix_invitation(prefix_id, email, status);