package com.revolut.interview.transactions;

import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.money.Money;
import com.revolut.interview.notification.NotificationService;
import com.revolut.interview.notification.TransactionNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.concurrent.Executor;

import static com.revolut.interview.transactions.TransactionState.FAILED;
import static com.revolut.interview.transactions.TransactionState.IN_PROGRESS;
import static com.revolut.interview.transactions.TransactionState.PENDING;
import static com.revolut.interview.transactions.TransactionState.SUCCEEDED;
import static java.util.Objects.requireNonNull;
import static javax.persistence.LockModeType.WRITE;

@Singleton
class TransactionExecutor {

    static final String MAX_ATTEMPTS = "MAX_ATTEMPTS";

    private static final Logger LOGGER = LogManager.getLogger();

    private final int maxAttempts;

    private final Executor transactionExecutor;

    private final Provider<Session> sessionProvider;

    private final AccountsDAO accountsDAO;
    private final TransactionDAO transactionDAO;

    private final NotificationService notificationService;
    private final BackoffStrategy backoffStrategy;

    @Inject
    TransactionExecutor(@Named(MAX_ATTEMPTS) int maxAttempts,
                        Executor transactionExecutor,
                        Provider<Session> sessionProvider,
                        AccountsDAO accountsDAO,
                        TransactionDAO transactionDAO,
                        NotificationService notificationService,
                        BackoffStrategy backoffStrategy) {
        this.maxAttempts = maxAttempts;
        this.transactionExecutor = transactionExecutor;
        this.sessionProvider = sessionProvider;
        this.accountsDAO = accountsDAO;
        this.transactionDAO = transactionDAO;
        this.notificationService = notificationService;
        this.backoffStrategy = backoffStrategy;
    }

    void execute(Transaction transaction) {
        requireNonNull(transaction, "Transaction cannot be null");
        transactionExecutor.execute(() -> executeTransaction(transaction));
    }

    private void executeTransaction(Transaction transaction) {
        LOGGER.debug("Executing transaction {}", transaction);

        var stateUpdatedSuccessfully = transactionDAO.updateState(transaction.getTransactionId(), PENDING, IN_PROGRESS);
        var transactionEntity = transactionDAO.findById(transaction.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction does not exist on database. ID: " + transaction.getTransactionId()));

        if (stateUpdatedSuccessfully) {
            executeTransaction(transactionEntity, 1);
        } else {
            LOGGER.error("Cannot execute transaction as transaction was not in pending state. State {}", transactionEntity.getTransactionState());
        }

        LOGGER.debug("Transaction execution completed {}", transaction);
    }

    private void executeTransaction(TransactionEntity transactionEntity, int attemptNumber) {
        var dbTransaction = sessionProvider.get()
                .beginTransaction();

        try {
            transferMoney(transactionEntity);
            dbTransaction.commit();
        } catch (StaleObjectStateException e) {
            LOGGER.error("Transaction could not be completed because account was updated.", e);

            dbTransaction.rollback();

            retryIfNeeded(transactionEntity, attemptNumber);
        } catch (Exception e) {
            LOGGER.error("An unhandled exception occurred while executing the transaction.", e);

            dbTransaction.rollback();

            transactionFailed(transactionEntity);
        }
    }

    private void retryIfNeeded(TransactionEntity transactionEntity, int attemptNumber) {
        if (attemptNumber < maxAttempts) {
            backoffStrategy.backOff(attemptNumber + 1);
            executeTransaction(transactionEntity, attemptNumber + 1);
        } else {
            transactionFailed(transactionEntity);
        }
    }

    private void transferMoney(TransactionEntity transactionEntity) {
        var amountToTransfer = transactionEntity.getAmount();
        var senderEntityOptional = accountsDAO.findById(transactionEntity.getSender().getId(), WRITE);
        var receiverEntityOptional = accountsDAO.findById(transactionEntity.getReceiver().getId(), WRITE);

        if (senderEntityOptional.isPresent() && receiverEntityOptional.isPresent()) {
            var sender = senderEntityOptional.get();
            var receiver = receiverEntityOptional.get();

            if (hasEnoughBalance(sender, amountToTransfer)) {
                transferAndUpdateAccounts(amountToTransfer, sender, receiver);
                transactionSuccessful(transactionEntity);
            } else {
                transactionFailed(transactionEntity);
            }
        } else {
            transactionFailed(transactionEntity);
        }
    }

    private void transferAndUpdateAccounts(BigDecimal amountToTransfer, AccountEntity sender, AccountEntity receiver) {
        var sendersNewBalance = sender.getBalance().subtract(amountToTransfer);
        var receiversNewBalance = receiver.getBalance().add(amountToTransfer);

        sender.setBalance(sendersNewBalance);
        receiver.setBalance(receiversNewBalance);

        accountsDAO.update(sender);
        accountsDAO.update(receiver);
    }

    private boolean hasEnoughBalance(AccountEntity sender, BigDecimal amountToTransfer) {
        return sender.getBalance().compareTo(amountToTransfer) >= 0;
    }

    private void transactionSuccessful(TransactionEntity transactionEntity) {
        transactionEntity.setTransactionState(SUCCEEDED);
        transactionDAO.update(transactionEntity);

        sendNotification(transactionEntity, true);
    }

    private void transactionFailed(TransactionEntity transactionEntity) {
        transactionEntity.setTransactionState(FAILED);
        transactionDAO.update(transactionEntity);

        sendNotification(transactionEntity, false);
    }

    private void sendNotification(TransactionEntity transactionEntity, boolean isSuccessful) {
        //An assumption is made that this will never throw any exception.
        notificationService.sendNotification(new TransactionNotification(
                        transactionEntity.getSender().getId(),
                        transactionEntity.getReceiver().getId(),
                        isSuccessful,
                        Money.valueOf(transactionEntity.getAmount())
                )
        );
    }
}
