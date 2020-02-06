package com.revolut.interview.transfer;

import com.revolut.interview.money.Money;

public class TransferRequest {

    private final long senderId;
    private final long receiverId;
    private final Money amountToTransfer;

    public TransferRequest(long senderId, long receiverId, Money amountToTransfer) {
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

    public Money getAmountToTransfer() {
        return amountToTransfer;
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
