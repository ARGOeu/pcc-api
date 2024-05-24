DELIMITER //

CREATE PROCEDURE UpdateResolvableCountPerPrefix (IN prfx nvarchar(30))
BEGIN

-- Declare variables to store count values
DECLARE count_resolvable INT;


-- Declare variables to store count values
DECLARE unresolvable_count INT;


-- Declare variables to store count values
DECLARE unchecked_count INT;

  -- Count rows in handles table based on the prefix value and specified conditions
SET @query = CONCAT('SELECT COUNT(*) INTO @count_resolvable FROM handles WHERE handle LIKE \'', prfx, '/%\' AND resolved = 1 AND LOWER(CONVERT(type using utf8))=\'url\'');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update resolvable_count in prefixes table for the current prefix
UPDATE prefixes SET resolvable_count = @count_resolvable WHERE prefix = prfx;

-- Count rows in handles table based on the prefix value and specified conditions
SET @query = CONCAT('SELECT COUNT(*) INTO @unresolvable_count FROM handles WHERE handle LIKE \'', prfx, '/%\' AND resolved = 0 AND LOWER(CONVERT(type using utf8))=\'url\'');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update unresolvable_count in prefixes table for the current prefix
UPDATE prefixes SET unresolvable_count = @unresolvable_count WHERE prefix = prfx;

-- Count rows in handles table based on the prefix value and specified conditions
SET @query = CONCAT('SELECT COUNT(*) INTO @unchecked_count FROM handles WHERE handle LIKE \'', prfx, '/%\' AND resolved = -1 AND LOWER(CONVERT(type using utf8))=\'url\'');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update unresolvable_count in prefixes table for the current prefix
UPDATE prefixes SET unchecked_count = @unchecked_count WHERE prefix = prfx;

END  //

DELIMITER ;
