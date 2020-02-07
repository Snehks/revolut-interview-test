package com.revolut.interview.money;

import java.math.BigDecimal;
import java.util.Objects;

public class Money {

    private final BigDecimal value;

    private Money(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money cannot be negative " + value);
        }

        this.value = value;
    }

    public static Money valueOf(double value) {
        return new Money(new BigDecimal(value));
    }

    public static Money valueOf(BigDecimal value) {
        return new Money(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Money add(Money money) {
        return new Money(value.add(money.value));
    }

    public Money subtract(Money money) {
        return new Money(value.subtract(money.value));
    }

    public boolean isLessThanZero() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isLessThanEqualToZero() {
        return value.compareTo(BigDecimal.ZERO) <= 0;
    }

    @Override
    public String toString() {
        return "Money{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(value, money.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
