package com.revolut.interview.transactions;

class InvalidTransactionException extends IllegalArgumentException {

    private final long transactionId;

    InvalidTransactionException(String message, long transactionId) {
        super(message);
        this.transactionId = transactionId;
    }
}
