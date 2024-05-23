DELIMITER //

create procedure UpdateResolvableCountForAuxHandles()
BEGIN
  DECLARE done INT DEFAULT FALSE;

  DECLARE handle_value VARCHAR(255);
  DECLARE prefix_value VARCHAR(255);

  -- Declare variables to store count values
  DECLARE count_resolvable INT;

  -- Declare variables to store count values
  DECLARE unresolvable_count INT;


  -- Declare variables to store count values
  DECLARE unchecked_count INT;

  -- Declare cursor to iterate through prefixes table
  DECLARE cursor_handles CURSOR FOR SELECT handle FROM aux_handles;

  -- Declare handler for cursor
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN cursor_handles;
  read_loop: LOOP
    FETCH cursor_handles INTO handle_value;
    IF done THEN
      LEAVE read_loop;
    END IF;


    SET prefix_value = SUBSTRING_INDEX(handle_value, '/', 1);
SELECT concat('my handle is ', handle_value);

SELECT concat('my prefix is ', prefix_value);
    -- Count rows in handles table based on the prefix value and specified conditions
    SET @query = CONCAT('SELECT COUNT(*) INTO @count_resolvable FROM handles WHERE handle LIKE \'', prefix_value, '/%\' AND resolved = 1 AND LOWER(CONVERT(type using utf8))=\'url\'');

SELECT concat('my query is ', @query);

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

  CLOSE cursor_handles;
END; //

DELIMITER ;
