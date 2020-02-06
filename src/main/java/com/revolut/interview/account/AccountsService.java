package com.revolut.interview.account;

import com.revolut.interview.money.Money;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class AccountsService {

    private final AccountsDAO accountsDAO;

    @Inject
    AccountsService(AccountsDAO accountsDAO) {
        this.accountsDAO = accountsDAO;
    }

    public Account getById(Long accountId) {
        return accountsDAO.findById(accountId)
                .map(this::map)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public Account save(Account account) {
        if (account.getBalance().isLessThanZero()) {
            throw new IllegalArgumentException("Money provided cannot be negative.");
        }

        var entityToSave = new AccountEntity(account.getBalance().getValue());
        var savedEntity = accountsDAO.save(entityToSave);

        return map(savedEntity);
    }

    private Account map(AccountEntity entity) {
        return new Account(
                entity.getId(),
                Money.valueOf(entity.getBalance())
        );
    }
}
