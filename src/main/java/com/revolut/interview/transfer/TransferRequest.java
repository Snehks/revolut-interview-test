package com.revolut.interview.transfer;

import java.math.BigDecimal;
import java.util.Objects;

public class TransferRequest {

    private final long senderId;
    private final long receiverId;
    private final BigDecimal amountToTransfer;

    TransferRequest(long senderId, long receiverId, BigDecimal amountToTransfer) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amountToTransfer = amountToTransfer;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferRequest)) return false;
        TransferRequest that = (TransferRequest) o;
        return senderId == that.senderId &&
                receiverId == that.receiverId &&
                amountToTransfer.compareTo(that.amountToTransfer) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, amountToTransfer);
    }

    @Override
    public String toString() {
        return "TransferRequestDTO{" +
                "senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", amountToTransfer='" + amountToTransfer + '\'' +
                '}';
    }
}
