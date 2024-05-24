DELIMITER //

CREATE PROCEDURE count_prefix_handles()
BEGIN
DECLARE finished INT DEFAULT 0;
DECLARE stored_prefix VARCHAR(20);
DECLARE cur CURSOR FOR SELECT prefix FROM prefixes;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;
DECLARE exit HANDLER FOR SQLEXCEPTION, SQLWARNING
BEGIN
ROLLBACK;
RESIGNAL;
END;
START TRANSACTION;
OPEN cur;
count: LOOP
FETCH cur INTO stored_prefix;
IF finished = 1 THEN
LEAVE count;
END IF;

SET @like_handles = concat(stored_prefix, '%');
SET @total_handles = (SELECT count(DISTINCT handle) FROM handles WHERE handle like @like_handles);

UPDATE prefixes SET handles_count = @total_handles where prefix = stored_prefix;

END LOOP;
CLOSE cur;
COMMIT;
END //

DELIMITER ;
