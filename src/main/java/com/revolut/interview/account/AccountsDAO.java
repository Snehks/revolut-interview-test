package com.revolut.interview.account;

import com.revolut.interview.persistence.AbstractDAO;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.LockModeType;
import java.util.Optional;

@Singleton
public class AccountsDAO extends AbstractDAO<AccountEntity> {

    @Inject
    AccountsDAO(Provider<Session> sessionProvider) {
        super(sessionProvider);
    }

    public Optional<AccountEntity> findById(Long id, LockModeType lockModeType) {
        return runInTransactionOrStartNewIfNotRunning(session ->
                Optional.ofNullable(
                        session.find(AccountEntity.class, id, lockModeType)
                )
        );
    }
}
