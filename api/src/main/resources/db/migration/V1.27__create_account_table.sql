-- ------------------------------------------------
-- Version: V1.27
-- Description: Create account table for prefix accounts
-- ------------------------------------------------

CREATE TABLE account (
    id VARCHAR(36) NOT NULL,
    prefix_id INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    admin_index INT NOT NULL,
    permissions VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_account_prefix
        FOREIGN KEY (prefix_id)
        REFERENCES prefix(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_account_prefix
    ON account(prefix_id);

CREATE INDEX idx_account_email
    ON account(email);