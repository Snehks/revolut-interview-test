package com.revolut.interview.transfer;

import com.google.gson.Gson;
import com.revolut.interview.rest.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TransferResource implements Resource {

    private static final Logger LOGGER = LogManager.getLogger();

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
        spark.post("/api/transfer", this::handleTransfer);

        spark.exception(AccountNotFoundException.class, getBadRequestHandler());
        spark.exception(InsufficientBalanceException.class, getBadRequestHandler());

        spark.after("/api/transfer/*", (request, response) -> response.type("application/json"));
    }

    private ExceptionHandler<Exception> getBadRequestHandler() {
        return (exception, request, response) -> {
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(exception.getMessage());
            LOGGER.error(exception);
        };
    }
}
