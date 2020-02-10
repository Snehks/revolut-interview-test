package com.revolut.interview.transactions;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.revolut.interview.rest.Resource;
import com.revolut.interview.transactions.BackoffStrategy.NOOPBackOffStrategy;

import java.util.concurrent.Executor;

import static com.google.inject.name.Names.named;
import static com.revolut.interview.transactions.TransactionExecutor.MAX_ATTEMPTS;

public class TransactionModule extends AbstractModule {

    @Override
    protected void configure() {
        //Max attempts are set hard-coded to 5. Ideally it should go on a config file.
        bindConstant()
                .annotatedWith(named(MAX_ATTEMPTS))
                .to(5);

        //Same thread executor. This is for the sake of the exercise. Ideally we would want it to be
        //a usual executor service.
        bind(Executor.class).toInstance(Runnable::run);

        //For the sake of this exercise, our backoff strategy implementation will do nothing.
        bind(BackoffStrategy.class).to(NOOPBackOffStrategy.class);

        Multibinder.newSetBinder(binder(), Resource.class)
                .addBinding()
                .to(TransactionResource.class);
    }
}
