package com.revolut.interview.account;

import com.revolut.interview.persistence.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "account")
public class AccountEntity extends BaseEntity {

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

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
                "balance=" + balance.stripTrailingZeros() +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountEntity)) return false;
        if (!super.equals(o)) return false;
        AccountEntity that = (AccountEntity) o;
        return Objects.equals(balance.stripTrailingZeros(), that.balance.stripTrailingZeros()) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balance.stripTrailingZeros(), version);
    }
}
