package com.revolut.interview.transfer;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.revolut.interview.rest.Resource;

public class TransfersModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Resource.class)
                .addBinding()
                .to(TransferResource.class);
    }
}
