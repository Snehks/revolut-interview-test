package com.revolut.interview.account;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.revolut.interview.rest.Resource;

public class AccountsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Resource.class)
                .addBinding()
                .to(AccountsResource.class);
    }
}
