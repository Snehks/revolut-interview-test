package com.revolut.interview.persistence;

import org.hibernate.Session;

import javax.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Function;

public class AbstractDAO<T extends BaseEntity> {

    protected final Provider<Session> sessionProvider;
    private final Class<T> entityType;

    protected AbstractDAO(Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
        this.entityType = getGenericClass();
    }

    public Optional<T> findById(Long id) {
        return runInTransactionOrStartNewIfNotRunning(session ->
                session.byId(entityType)
                        .loadOptional(id)
        );
    }

    public T save(T t) {
        return runInTransactionOrStartNewIfNotRunning(session -> {
            var id = session.save(t);
            t.setId((Long) id);

            return t;
        });
    }

    public void update(T entity) {
        runInTransactionOrStartNewIfNotRunning(session -> {
            session.update(entity);
            return entity;
        });
    }

    protected <R> R runInTransactionOrStartNewIfNotRunning(Function<Session, R> returningTask) {
        var session = sessionProvider.get();
        var transaction = session.getTransaction();

        if (transaction.isActive()) {
            return returningTask.apply(session);
        } else {
            transaction.begin();
            var result = returningTask.apply(session);
            transaction.commit();

            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getGenericClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
