package com.revolut.interview;

import com.google.inject.AbstractModule;
import com.revolut.interview.account.AccountsModule;
import com.revolut.interview.persistence.PersistenceModule;
import com.revolut.interview.rest.SparkRestModule;
import com.revolut.interview.transactions.TransactionModule;
import com.revolut.interview.transfer.TransferModule;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AccountsModule());
        install(new TransferModule());
        install(new TransactionModule());
        install(new PersistenceModule());
        install(new SparkRestModule());
    }
}
