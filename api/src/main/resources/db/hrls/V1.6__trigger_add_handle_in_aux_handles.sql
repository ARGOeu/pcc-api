DELIMITER |
create trigger trigger_add_handle_in_aux_handles after insert on handles
for each row
begin
insert into aux_handles(handle) values(NEW.handle);
end;
|

DELIMITER ;