package com.revolut.interview.rest;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import spark.Service;

public class SparkRestModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger();

    @Provides
    @Singleton
    Service provideSpark(Gson gson) {
        var spark = Service.ignite();

        spark.defaultResponseTransformer(gson::toJson);

        spark.exception(IllegalArgumentException.class, (exception, request, response) -> {
            LOGGER.error(exception);
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(exception.getMessage());
        });

        return spark;
    }

    @Singleton
    @Provides
    public Gson provideGson() {
        return new Gson();
    }
}
