package com.revolut.interview.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
class SessionProvider implements Provider<Session> {

    private final SessionFactory sessionFactory;

    @Inject
    SessionProvider(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Session get() {
        return sessionFactory.getCurrentSession();
    }
}
