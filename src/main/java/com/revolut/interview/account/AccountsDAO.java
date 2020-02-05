package com.revolut.interview.account;

import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class AccountsDAO {

    private final Provider<Session> sessionProvider;

    @Inject
    public AccountsDAO(Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public Optional<AccountEntity> findById(Long id) {
        return runInTransaction(session ->
                session.byId(AccountEntity.class)
                        .loadOptional(id)
        );
    }

    public Optional<AccountEntity> findById(Long id, LockModeType lockModeType) {
        return runInTransaction(session ->
                Optional.ofNullable(
                        session.find(AccountEntity.class, id, lockModeType)
                )
        );
    }

    public void update(AccountEntity entity) {
        runInTransaction(session -> {
            session.update(entity);
            return null;
        });
    }

    public AccountEntity save(AccountEntity accountEntity) {
        return runInTransaction(session -> {
            var id = session.save(accountEntity);
            accountEntity.setId((Long) id);

            return accountEntity;
        });
    }

    private <T> T runInTransaction(Function<Session, T> task) {
        var session = sessionProvider.get();
        var transaction = session.getTransaction();

        if (transaction.isActive()) {
            return task.apply(session);
        } else {
            transaction.begin();
            var result = task.apply(session);
            transaction.commit();

            return result;
        }
    }
}
