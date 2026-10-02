package gr.grnet.pccapi.mapper;

import gr.grnet.pccapi.dto.CodelistDto;
import gr.grnet.pccapi.dto.account.AccountRequestDto;
import gr.grnet.pccapi.dto.account.AccountResponseDto;
import gr.grnet.pccapi.entity.Account;
import gr.grnet.pccapi.entity.Codelist;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    List<AccountResponseDto> accountToResponseDto(List<Account> account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prefix", ignore = true)
    @Mapping(target = "adminIndex", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account accountRequestToEntity(AccountRequestDto dto);

    @Mapping(target = "prefixId", source = "prefix.id")
    AccountResponseDto accountToResponseDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prefix", ignore = true)
    @Mapping(target = "adminIndex", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateAccountEntityFromDto(AccountRequestDto dto, @MappingTarget Account account);
}