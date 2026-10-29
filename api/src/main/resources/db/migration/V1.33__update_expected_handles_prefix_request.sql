-- ------------------------------------------------
-- Version: v1.32
--
-- Description: Adds FROM_500000_TO_1000000 to prefix_request expected_handles values
-- ------------------------------------------------
ALTER TABLE prefix_request
DROP CHECK chk_prefix_request_expected_handles;

ALTER TABLE prefix_request
    ADD CONSTRAINT chk_prefix_request_expected_handles
        CHECK (
            expected_handles IS NULL
                OR expected_handles IN (
                                        'UP_TO_1000',
                                        'FROM_1000_TO_10000',
                                        'FROM_10000_TO_500000',
                                        'FROM_500000_TO_1000000',
                                        'OVER_1000000'
                )
            );