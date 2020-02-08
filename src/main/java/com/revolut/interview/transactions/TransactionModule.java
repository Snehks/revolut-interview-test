package com.revolut.interview.transactions;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.revolut.interview.rest.Resource;

import java.util.concurrent.Executor;

public class TransactionModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant()
                .annotatedWith(Names.named(TransactionExecutor.MAX_ATTEMPTS))
                .to(5);

        bind(Executor.class).toInstance(Runnable::run);
        bind(BackoffStrategy.class).to(BackoffStrategy.NOOPBackOffStrategy.class);

        Multibinder.newSetBinder(binder(), Resource.class)
                .addBinding()
                .to(TransactionResource.class);
    }
}
