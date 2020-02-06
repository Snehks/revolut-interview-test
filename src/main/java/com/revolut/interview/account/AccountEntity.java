package com.revolut.interview.account;

import com.revolut.interview.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "account")
public class AccountEntity extends BaseEntity {

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public AccountEntity() {
    }

    public AccountEntity(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "balance=" + balance +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountEntity)) return false;
        if (!super.equals(o)) return false;
        AccountEntity that = (AccountEntity) o;
        return balance.stripTrailingZeros().equals(that.balance.stripTrailingZeros());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balance);
    }
}
