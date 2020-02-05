package com.revolut.interview.transfer;

import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TransferLogDAO {

    private final Provider<Session> sessionProvider;

    @Inject
    TransferLogDAO(Provider<Session> sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    void save(TransferLogEntity entityToSave) {
        var session = sessionProvider.get();
        var transaction = session.getTransaction();

        if (transaction.isActive()) {
            session.save(entityToSave);
        } else {
            transaction.begin();
            session.save(entityToSave);
            transaction.commit();
        }
    }
}
