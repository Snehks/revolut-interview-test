package com.revolut.interview.money;

import java.math.BigDecimal;
import java.util.Objects;

public class MoneyDTO {

    private final BigDecimal value;

    public MoneyDTO(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MoneyDTO{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoneyDTO moneyDTO = (MoneyDTO) o;
        return Objects.equals(value, moneyDTO.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
