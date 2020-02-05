package com.revolut.interview;

import com.google.inject.Guice;
import com.revolut.interview.transfer.TransferService;

public class TransferServiceStarter {

    public static void main(String[] args) {
        var injector = Guice.createInjector(new ApplicationModule());
        var moneyTransferService = injector.getInstance(TransferService.class);
    }
}
