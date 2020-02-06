package com.revolut.interview;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.revolut.interview.rest.Resource;
import io.javalin.Javalin;

import java.util.Set;

import static java.lang.Runtime.getRuntime;

public class TransferServiceStarter {

    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        var injector = Guice.createInjector(new ApplicationModule());

        var javalin = injector.getInstance(Javalin.class);

        var allResources = injector.getInstance(Key.get(new TypeLiteral<Set<Resource>>() {
        }));

        allResources.forEach(allResource -> allResource.register(javalin));

        getRuntime().addShutdownHook(new Thread(javalin::stop));

        javalin.start(7000);
    }
}
