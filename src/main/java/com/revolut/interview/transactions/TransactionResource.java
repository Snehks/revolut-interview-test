package com.revolut.interview.transactions;

import com.revolut.interview.rest.Resource;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TransactionResource implements Resource {

    private final TransactionService transactionService;

    @Inject
    TransactionResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void register(Service spark) {
        spark.get("/transactions/:accountId", this::getAllTransactions);
    }

    private Iterable<Transaction> getAllTransactions(Request request, Response response) {
        var accountId = Long.parseLong(request.params("accountId"));

        return transactionService.getAllTransactionsForAccountId(accountId);
    }
}
