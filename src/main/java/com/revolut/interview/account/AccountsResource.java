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

    private static final String BASE_PATH = "/api/account";

    private final AccountsService accountsService;
    private final Gson gson;

    @Inject
    AccountsResource(AccountsService accountsService, Gson gson) {
        this.accountsService = accountsService;
        this.gson = gson;
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

    @Override
    public void register(Service spark) {
        spark.get(BASE_PATH + "/:id", this::getAccount);
        spark.post(BASE_PATH, this::addAccount);

        spark.after(BASE_PATH + "/*", (request, response) -> response.type("application/json"));
    }
}
