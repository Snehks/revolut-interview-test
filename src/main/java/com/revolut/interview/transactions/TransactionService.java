package com.revolut.interview.transactions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TransactionService {

    private final TransactionExecutor transactionExecutor;
    private final TransactionDAO transactionDAO;

    @Inject
    TransactionService(TransactionExecutor transactionExecutor, TransactionDAO transactionDAO) {
        this.transactionExecutor = transactionExecutor;
        this.transactionDAO = transactionDAO;
    }

    public List<Transaction> getAllTransactionsForAccountId(long accountId) {
        //This returns all the records where accountId is sender or receiver. Not very scalable.
        return transactionDAO.findAllWithAccountId(accountId)
                .stream()
                .map(this::map)
                .collect(Collectors.toUnmodifiableList());
    }

    public void queue(long transactionId) {
        var transactionEntity = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new InvalidTransactionException("No transaction with id " + transactionId, transactionId));

        if (transactionEntity.getTransactionState() != TransactionState.PENDING) {
            throw new InvalidTransactionException("Trying to queue a transaction which is not pending " + transactionEntity.getTransactionState(), transactionId);
        }

        transactionExecutor.execute(map(transactionEntity));
    }

    private Transaction map(TransactionEntity transactionEntity) {
        return new Transaction(
                transactionEntity.getId(),
                transactionEntity.getSender().getId(),
                transactionEntity.getReceiver().getId(),
                transactionEntity.getAmount(),
                transactionEntity.getTransactionState()
        );
    }
}
