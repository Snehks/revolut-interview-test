package com.revolut.interview.transactions;

import com.revolut.interview.persistence.AbstractDAO;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TransactionDAO extends AbstractDAO<TransactionEntity> {

    @Inject
    TransactionDAO(Provider<Session> sessionProvider) {
        super(sessionProvider);
    }
}
