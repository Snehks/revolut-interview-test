package com.revolut.interview;

import com.google.inject.AbstractModule;
import com.revolut.interview.account.AccountsModule;
import com.revolut.interview.persistence.PersistenceModule;
import com.revolut.interview.rest.JavalinRestModule;
import com.revolut.interview.transfer.TransferModule;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AccountsModule());
        install(new TransferModule());
        install(new PersistenceModule());
        install(new JavalinRestModule());
    }
}
