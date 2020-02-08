package com.revolut.interview.account;

import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Account {

    private final Long id;
    private final BigDecimal balance;

    public Account(Long id, BigDecimal balance) {
        this.id = id;
        this.balance = requireNonNull(balance, "Balance cannot be null.");
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isBalanceLessThanZero() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                balance.compareTo(account.balance) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance);
    }
}
