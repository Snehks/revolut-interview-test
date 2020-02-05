package com.revolut.interview.transfer;

import com.revolut.interview.persistence.AbstractDAO;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TransferLogDAO extends AbstractDAO<TransferLogEntity> {

    @Inject
    TransferLogDAO(Provider<Session> sessionProvider) {
        super(sessionProvider);
    }
}
