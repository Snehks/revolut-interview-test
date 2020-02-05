package com.revolut.interview.account;

import com.revolut.interview.money.Money;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class AccountDTO {

    private final Long id;
    private final Money balance;

    public AccountDTO(Long id, Money balance) {
        this.id = requireNonNull(id, "Id cannot be null.");
        this.balance = requireNonNull(balance, "Balance cannot be null.");
    }

    public Long getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDTO that = (AccountDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, balance);
    }
}
