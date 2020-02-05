package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.persistence.Entity;

import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "transfer_log")
public class TransferLogEntity extends Entity {

    private AccountEntity sender;
    private AccountEntity receiver;
    private BigDecimal amount;

    public TransferLogEntity() {
    }

    public TransferLogEntity(AccountEntity senderEntity, AccountEntity receiverEntity, BigDecimal amount) {
        this.sender = senderEntity;
        this.receiver = receiverEntity;
        this.amount = amount;
    }

    public AccountEntity getSender() {
        return sender;
    }

    public void setSender(AccountEntity sender) {
        this.sender = sender;
    }

    public AccountEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(AccountEntity receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
