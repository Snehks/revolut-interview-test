package com.revolut.interview.transactions;

import com.revolut.interview.rest.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TransactionResource implements Resource {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TransactionService transactionService;

    @Inject
    TransactionResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private Iterable<Transaction> getAllTransactions(Request request, Response response) {
        var accountId = Long.parseLong(request.params("accountId"));

        return transactionService.getAllTransactionsForAccountId(accountId);
    }

    @Override
    public void register(Service spark) {
        spark.get("/transactions/:accountId", this::getAllTransactions);

        spark.exception(InvalidTransactionException.class, (exception, request, response) -> {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            LOGGER.error("An internal server error occurred.", exception);
        });
    }
}
