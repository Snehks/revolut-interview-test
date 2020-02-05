package com.revolut.interview.persistence;

import org.hibernate.Session;

import javax.inject.Provider;
import java.util.function.Function;

public class AbstractDAO<T extends Entity> {

    protected final Provider<Session> sessionProvider;

    protected AbstractDAO(Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public T save(T t) {
        return runInTransaction(session -> {
            var id = session.save(t);
            t.setId((Long) id);

            return t;
        });
    }

    protected <R> R runInTransaction(Function<Session, R> task) {
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
