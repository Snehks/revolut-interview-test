package com.revolut.interview.account;

public class AccountNotFoundException extends IllegalArgumentException {

    private final Long id;

    public AccountNotFoundException(Long id) {
        super("Could not find account with id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
