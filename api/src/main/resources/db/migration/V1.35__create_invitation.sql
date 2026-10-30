-- ------------------------------------------------
-- Version: V1.34
-- Description: Generalize prefix invitations for API resources
-- ------------------------------------------------

CREATE TABLE invitation (
    id VARCHAR(36) NOT NULL,
    api_resource VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    created_by VARCHAR(255),
    responded_by VARCHAR(255),

    PRIMARY KEY (id)
);

INSERT INTO invitation (
    id,
    api_resource,
    resource_id,
    email,
    role,
    status,
    created_at,
    responded_at,
    created_by,
    responded_by
)
SELECT
    id,
    'Prefix',
    CAST(prefix_id AS CHAR),
    email,
    role,
    status,
    created_at,
    responded_at,
    created_by,
    responded_by
FROM prefix_invitation;

DROP TABLE prefix_invitation;

CREATE INDEX idx_invitation_target
    ON invitation(api_resource, resource_id);

CREATE INDEX idx_invitation_lookup
    ON invitation(api_resource, resource_id, email, status);

CREATE INDEX idx_invitation_email
    ON invitation(email);