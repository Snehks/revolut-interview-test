package com.revolut.interview.transactions;

import java.math.BigDecimal;

public class Transaction {

    private final long transactionId;
    private final long senderId;
    private final long receiverId;
    private final BigDecimal amountToTransfer;

    public Transaction(long transactionId, long senderId, long receiverId, BigDecimal amountToTransfer, TransactionState transactionState) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amountToTransfer = amountToTransfer;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public BigDecimal getAmountToTransfer() {
        return amountToTransfer;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", amountToTransfer=" + amountToTransfer +
                '}';
    }
}
