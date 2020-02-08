package com.revolut.interview.transfer;

import java.math.BigDecimal;

class InsufficientBalanceException extends IllegalArgumentException {

    InsufficientBalanceException(BigDecimal currentBalance, BigDecimal amountToTransfer) {
        super("Insufficient balance. Current $" + currentBalance + ". Trying to transfer $" + amountToTransfer);
    }
}
