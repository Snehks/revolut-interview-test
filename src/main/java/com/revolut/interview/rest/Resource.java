package com.revolut.interview.rest;

import io.javalin.Javalin;

public interface Resource {

    void register(Javalin javalin);
}
