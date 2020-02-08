package com.revolut.interview.notification;

import com.revolut.interview.money.Money;

import java.util.Objects;

public class TransactionNotification {

    final long senderId;
    final long receiverId;
    final boolean success;
    final Money amount;

    public TransactionNotification(long senderId, long receiverId, boolean success, Money amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.success = success;
        this.amount = amount;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Money getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionNotification)) return false;
        TransactionNotification that = (TransactionNotification) o;
        return senderId == that.senderId &&
                receiverId == that.receiverId &&
                success == that.success &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, success, amount);
    }
}
