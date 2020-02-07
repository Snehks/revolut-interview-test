package com.revolut.interview.transfer;

import com.google.gson.Gson;
import com.revolut.interview.rest.Resource;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TransferResource implements Resource {

    private final TransferService transferService;
    private final Gson gson;

    @Inject
    TransferResource(TransferService transferService, Gson gson) {
        this.transferService = transferService;
        this.gson = gson;
    }

    private String handleTransfer(Request request, Response response) {
        var transferRequestDTO = gson.fromJson(request.body(), TransferRequest.class);
        transferService.transfer(transferRequestDTO);

        return "Your money transfer request has been submitted.";
    }

    @Override
    public void register(Service spark) {
        spark.post("/transfer", this::handleTransfer);
    }
}
