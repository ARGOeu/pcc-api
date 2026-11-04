-- ------------------------------------------------
-- Version: v1.35
--
-- Description: Create table to store the latest
-- EPIC_STATS value for each prefix
-- ------------------------------------------------

CREATE TABLE prefix_epic_stats (
    prefix_id INT NOT NULL PRIMARY KEY,
    stats_value BIGINT NULL,
    handle_timestamp DATETIME(6) NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    last_checked_at DATETIME(6) NULL,
    last_success_at DATETIME(6) NULL,

    CONSTRAINT fk_prefix_epic_stats_prefix
        FOREIGN KEY (prefix_id)
        REFERENCES prefix(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_prefix_epic_stats_status
        CHECK (status IN (
            'PENDING',
            'FOUND',
            'NOT_FOUND',
            'NO_STATS',
            'ERROR'
        ))
);