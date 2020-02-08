package com.revolut.interview.account;

import com.google.gson.Gson;
import com.revolut.interview.rest.Resource;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class AccountsResource implements Resource {

    private final AccountsService accountsService;
    private final Gson gson;

    @Inject
    AccountsResource(AccountsService accountsService, Gson gson) {
        this.accountsService = accountsService;
        this.gson = gson;
    }

    @Override
    public void register(Service spark) {
        spark.get("/account/:id", this::getAccount);
        spark.post("/account", this::addAccount);

        spark.after("/account/*", (request, response) -> response.type("application/json"));
    }

    private Account getAccount(Request request, Response response) {
        var accountId = Long.valueOf(request.params("id"));

        var account = accountsService.getById(accountId);

        if (account.isPresent()) {
            return account.get();
        }

        response.status(HttpStatus.NOT_FOUND_404);

        return null;
    }

    private Account addAccount(Request request, Response response) {
        var accountToSave = gson.fromJson(request.body(), Account.class);
        return accountsService.save(accountToSave);
    }
}
