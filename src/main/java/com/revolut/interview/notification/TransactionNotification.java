package com.revolut.interview.notification;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionNotification {

    final long senderId;
    final long receiverId;
    final boolean success;
    final BigDecimal amount;

    public TransactionNotification(long senderId, long receiverId, boolean success, BigDecimal amount) {
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

    public BigDecimal getAmount() {
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
                this.amount.compareTo(that.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, success, amount);
    }

    @Override
    public String toString() {
        return "TransactionNotification{" +
                "senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", success=" + success +
                ", amount=" + amount +
                '}';
    }
}
