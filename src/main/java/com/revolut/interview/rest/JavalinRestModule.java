package com.revolut.interview.rest;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;

public class JavalinRestModule extends AbstractModule {

    @Provides
    @Singleton
    Javalin provideJavalin(Gson gson) {
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        return Javalin.create();
    }
}
