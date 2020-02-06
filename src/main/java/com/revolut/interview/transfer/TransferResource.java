package com.revolut.interview.transfer;

import com.revolut.interview.rest.Resource;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TransferResource implements Resource {

    private final TransferService transferService;

    @Inject
    TransferResource(TransferService transferService) {
        this.transferService = transferService;
    }

    void handle(Context ctx) {
        var transferRequestDTO = ctx.bodyAsClass(TransferRequest.class);

        transferService.transfer(transferRequestDTO);
    }

    @Override
    public void register(Javalin javalin) {
        javalin.post("/transfer", this::handle);
    }
}
