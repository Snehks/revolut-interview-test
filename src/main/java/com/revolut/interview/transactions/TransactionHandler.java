package com.revolut.interview.transactions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionHandler {

    private final TransactionExecutor transactionExecutor;
    private final TransactionDAO transactionDAO;

    @Inject
    TransactionHandler(TransactionExecutor transactionExecutor, TransactionDAO transactionDAO) {
        this.transactionExecutor = transactionExecutor;
        this.transactionDAO = transactionDAO;
    }

    public void queue(long transactionId) {
        var transactionEntity = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("No transaction with id " + transactionId));

        if (transactionEntity.getTransactionState() != TransactionState.PENDING) {
            throw new IllegalArgumentException("Trying to queue a transaction which is not pending " + transactionEntity.getTransactionState());
        }

        transactionExecutor.execute(map(transactionEntity));
    }

    private Transaction map(TransactionEntity transactionEntity) {
        return new Transaction(
                transactionEntity.getId(),
                transactionEntity.getSender().getId(),
                transactionEntity.getReceiver().getId(),
                transactionEntity.getAmount()
        );
    }
}
