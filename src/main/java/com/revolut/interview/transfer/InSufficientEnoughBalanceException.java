package com.revolut.interview.transfer;

import java.math.BigDecimal;

public class InSufficientEnoughBalanceException extends IllegalArgumentException {

    public InSufficientEnoughBalanceException(BigDecimal balance, BigDecimal value) {

    }
}
