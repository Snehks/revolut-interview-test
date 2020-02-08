package com.revolut.interview.transfer;

class AccountNotFoundException extends IllegalArgumentException {

    private final Long id;

    AccountNotFoundException(Long id) {
        super("Could not find account with id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
