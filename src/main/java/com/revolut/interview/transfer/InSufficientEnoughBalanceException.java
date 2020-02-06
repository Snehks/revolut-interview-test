package com.revolut.interview.transfer;

import java.math.BigDecimal;

public class InSufficientEnoughBalanceException extends IllegalArgumentException {

    public InSufficientEnoughBalanceException(BigDecimal balance, BigDecimal amountToTransfer) {
        super("Balance is insufficient. Required [" + amountToTransfer + "], found [" + balance + "]");
    }
}
