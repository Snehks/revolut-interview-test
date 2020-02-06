package com.revolut.interview.transactions;

import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.math.BigDecimal;

@Entity(name = "transactions")
public class TransactionEntity extends BaseEntity {

    @JoinColumn(name = "sender_id")
    @OneToOne
    private AccountEntity sender;

    @JoinColumn(name = "receiver_id")
    @OneToOne
    private AccountEntity receiver;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    public TransactionEntity() {
    }

    public TransactionEntity(AccountEntity senderEntity, AccountEntity receiverEntity, BigDecimal amount) {
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
