package com.revolut.interview.transfer;

import java.math.BigDecimal;

class InSufficientEnoughBalanceException extends IllegalArgumentException {

    InSufficientEnoughBalanceException(BigDecimal balance, BigDecimal amountToTransfer) {
        super("Balance is insufficient. Required [" + amountToTransfer + "], found [" + balance + "]");
    }
}
