package com.revolut.interview;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.revolut.interview.rest.Resource;
import spark.Service;

import java.util.Set;

import static java.lang.Runtime.getRuntime;

public class TransferServiceStarter {

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        var injector = Guice.createInjector(new ApplicationModule());

        var spark = injector.getInstance(Service.class);

        spark.port(8000);

        var allResources = injector.getInstance(Key.get(new TypeLiteral<Set<Resource>>() {
        }));

        allResources.forEach(resource -> resource.register(spark));

        getRuntime().addShutdownHook(new Thread(spark::stop));
    }
}
