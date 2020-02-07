package com.revolut.interview.transfer;

public class TransferResponse {

    private final Long transactionId;
    private final boolean queued;
    private final String description;

    private TransferResponse(Long transactionId, boolean queued, String description) {
        this.transactionId = transactionId;
        this.queued = queued;
        this.description = description;
    }

    public static TransferResponse success(long transactionId, String description) {
        return new TransferResponse(transactionId, true, description);
    }

    public static TransferResponse failure(String description) {
        return new TransferResponse(null, false, description);
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public boolean isQueued() {
        return queued;
    }

    public String getDescription() {
        return description;
    }
}
