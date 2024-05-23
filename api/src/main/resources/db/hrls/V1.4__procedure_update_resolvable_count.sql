DELIMITER //

CREATE PROCEDURE UpdateResolvableCount()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE prefix_value VARCHAR(255);

  -- Declare variables to store count values
  DECLARE count_resolvable INT;


  -- Declare variables to store count values
  DECLARE unresolvable_count INT;


  -- Declare variables to store count values
  DECLARE unchecked_count INT;

  -- Declare cursor to iterate through prefixes table
  DECLARE cursor_prefixes CURSOR FOR SELECT prefix FROM prefixes;

  -- Declare handler for cursor
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN cursor_prefixes;
  read_loop: LOOP
    FETCH cursor_prefixes INTO prefix_value;
    IF done THEN
      LEAVE read_loop;
    END IF;

    -- Count rows in handles table based on the prefix value and specified conditions
    SET @query = CONCAT('SELECT COUNT(*) INTO @count_resolvable FROM handles WHERE handle LIKE \'', prefix_value, '/%\' AND resolved = 1 AND LOWER(CONVERT(type using utf8))=\'url\'');
    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Update resolvable_count in prefixes table for the current prefix
    UPDATE prefixes
    SET resolvable_count = @count_resolvable
    WHERE prefix = prefix_value;

    -- Count rows in handles table based on the prefix value and specified conditions
    SET @query = CONCAT('SELECT COUNT(*) INTO @unresolvable_count FROM handles WHERE handle LIKE \'', prefix_value, '/%\' AND resolved = 0 AND LOWER(CONVERT(type using utf8))=\'url\'');
    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Update unresolvable_count in prefixes table for the current prefix
    UPDATE prefixes
    SET unresolvable_count = @unresolvable_count
    WHERE prefix = prefix_value;

    -- Count rows in handles table based on the prefix value and specified conditions
    SET @query = CONCAT('SELECT COUNT(*) INTO @unchecked_count FROM handles WHERE handle LIKE \'', prefix_value, '/%\' AND resolved = -1 AND LOWER(CONVERT(type using utf8))=\'url\'');
    PREPARE stmt FROM @query;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Update unresolvable_count in prefixes table for the current prefix
    UPDATE prefixes
    SET unchecked_count = @unchecked_count
    WHERE prefix = prefix_value;
  END LOOP;

  CLOSE cursor_prefixes;
END; //

DELIMITER ;
