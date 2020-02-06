package com.revolut.interview.account;

import com.revolut.interview.rest.Resource;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class AccountsResource implements Resource {

    private final AccountsService accountsService;

    @Inject
    AccountsResource(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    void getAccount(Context context) {
        var accountId = Long.valueOf(context.pathParam("id"));
        var savedAccount = accountsService.getById(accountId);

        context.json(savedAccount);
    }

    void addAccount(Context context) {
        var accountToSave = context.bodyAsClass(Account.class);
        var savedAccount = accountsService.save(accountToSave);

        context.json(savedAccount);
    }

    @Override
    public void register(Javalin javalin) {
        javalin.get("/account/:id", this::getAccount);
        javalin.post("/account", this::addAccount);
    }
}
