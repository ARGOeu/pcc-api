DELIMITER |
create trigger trigger_add_handle_in_aux_handles after insert on handles
for each row
begin
if LOWER(CONVERT(NEW.type using utf8))='url' then
insert into aux_handles(handle) values(NEW.handle);
end if;
end;
|

DELIMITER ;