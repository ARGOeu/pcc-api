package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.account.AccountRequestDto;
import gr.grnet.pccapi.dto.account.AccountResponseDto;
import gr.grnet.pccapi.dto.pagination.PageResource;
import gr.grnet.pccapi.mapper.AccountMapper;
import gr.grnet.pccapi.repository.AccountRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@ApplicationScoped
public class AccountService {

    private static final Integer ADMIN_INDEX = 301;
    private static final String PERMISSIONS = "011111110011";

    @Inject
    AccountRepository accountRepository;

    @Inject
    PrefixRepository prefixRepository;

    @Transactional
    public AccountResponseDto createAccount(
            Integer providerId,
            Integer prefixId,
            AccountRequestDto request) {

        var prefix = prefixRepository
                .findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Prefix with id " + prefixId +
                                " under Provider with id " + providerId
                ));

        var account = AccountMapper.INSTANCE.accountRequestToEntity(request);

        account.prefix = prefix;
        account.email = request.email.trim().toLowerCase();
        account.adminIndex = ADMIN_INDEX;
        account.permissions = PERMISSIONS;

        accountRepository.persist(account);

        return AccountMapper.INSTANCE.accountToResponseDto(account);
    }

    public PageResource<AccountResponseDto> getAccounts(Integer providerId, Integer prefixId, String search, int page, int size, UriInfo uriInfo) {

        validatePrefix(providerId, prefixId);

        var accounts = accountRepository.fetchAccountsByPrefixIdAndPage(prefixId, search, page, size);

        return new PageResource<>(accounts, AccountMapper.INSTANCE.accountToResponseDto(accounts.list()), uriInfo);

    }

    public AccountResponseDto getAccountById(Integer providerId, Integer prefixId, String accountId) {

        validatePrefix(providerId, prefixId);

        var account = accountRepository
                .findByIdAndPrefixId(accountId, prefixId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Account with id " + accountId +
                                " under Prefix with id " + prefixId
                ));

        return AccountMapper.INSTANCE.accountToResponseDto(account);
    }

    @Transactional
    public AccountResponseDto updateAccount(
            Integer providerId,
            Integer prefixId,
            String accountId,
            AccountRequestDto request) {

        validatePrefix(providerId, prefixId);

        var account = accountRepository
                .findByIdAndPrefixId(accountId, prefixId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Account with id " + accountId +
                                " under Prefix with id " + prefixId
                ));

        AccountMapper.INSTANCE.updateAccountEntityFromDto(request, account);

        account.email = account.email.trim().toLowerCase();

        return AccountMapper.INSTANCE.accountToResponseDto(account);
    }

    @Transactional
    public void deleteAccount(
            Integer providerId,
            Integer prefixId,
            String accountId) {

        validatePrefix(providerId, prefixId);

        var account = accountRepository
                .findByIdAndPrefixId(accountId, prefixId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Account with id " + accountId +
                                " under Prefix with id " + prefixId
                ));

        accountRepository.delete(account);
    }

    private void validatePrefix(Integer providerId, Integer prefixId) {

        prefixRepository.findByIdAndProviderId(prefixId, providerId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Prefix with id " + prefixId +
                                " under Provider with id " + providerId
                ));
    }
}