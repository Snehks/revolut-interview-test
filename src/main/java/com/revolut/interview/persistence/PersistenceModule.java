package com.revolut.interview.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.transactions.TransactionEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.inject.Singleton;

public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Session.class).toProvider(SessionProvider.class);
    }

    @Provides
    @Singleton
    SessionFactory sessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        configuration.addAnnotatedClass(AccountEntity.class);
        configuration.addAnnotatedClass(TransactionEntity.class);

        return configuration.buildSessionFactory();
    }
}
