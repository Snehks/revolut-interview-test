package com.revolut.interview.account;

import com.revolut.interview.money.Money;

import static java.util.Objects.requireNonNull;

public class Account {

    private final Long id;
    private final Money balance;

    public Account(Long id, Money balance) {
        this.id = id;
        this.balance = requireNonNull(balance, "Balance cannot be null.");
    }

    public Long getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }
}
