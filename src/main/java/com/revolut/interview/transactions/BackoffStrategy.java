package com.revolut.interview.transactions;

import javax.inject.Singleton;

public interface BackoffStrategy {

    void backOff(int attemptNumber);

    @Singleton
    class NOOPBackOffStrategy implements BackoffStrategy {

        @Override
        public void backOff(int attemptNumber) {
            //No Implementation required
        }
    }
}
