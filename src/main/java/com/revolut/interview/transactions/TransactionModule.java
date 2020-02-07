package com.revolut.interview.transactions;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.util.concurrent.Executor;

public class TransactionModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant()
                .annotatedWith(Names.named(TransactionExecutor.MAX_ATTEMPTS))
                .to(5);

        bind(Executor.class).toInstance(Runnable::run);
    }
}
