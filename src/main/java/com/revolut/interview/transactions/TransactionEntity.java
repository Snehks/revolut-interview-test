package com.revolut.interview.transactions;

import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "transactions")
public class TransactionEntity extends BaseEntity {

    @JoinColumn(name = "sender_id", nullable = false)
    @OneToOne
    private AccountEntity sender;

    @JoinColumn(name = "receiver_id", nullable = false)
    @OneToOne
    private AccountEntity receiver;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TransactionState transactionState;

    public TransactionEntity() {
    }

    public TransactionEntity(AccountEntity senderEntity, AccountEntity receiverEntity, BigDecimal amount, TransactionState transactionState) {
        this.sender = senderEntity;
        this.receiver = receiverEntity;
        this.amount = amount;
        this.transactionState = transactionState;
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

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionEntity)) return false;
        if (!super.equals(o)) return false;
        TransactionEntity that = (TransactionEntity) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(receiver, that.receiver) &&
                amount.compareTo(that.amount) == 0 &&
                transactionState == that.transactionState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sender, receiver, amount, transactionState);
    }
}
