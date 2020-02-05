package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountDTO;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountNotFoundException;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.money.Money;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;
import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;

@Singleton
public class TransferService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Provider<Session> sessionProvider;
    private final AccountsDAO accountsDAO;
    private final TransferLogDAO transferLogDAO;

    @Inject
    TransferService(Provider<Session> sessionProvider, AccountsDAO accountsDAO, TransferLogDAO transferLogDAO) {
        this.sessionProvider = sessionProvider;
        this.accountsDAO = accountsDAO;
        this.transferLogDAO = transferLogDAO;
    }

    public void transfer(AccountDTO sender, AccountDTO receiver, Money moneyToTransfer) {
        checkValidArgs(sender, receiver, moneyToTransfer);

        LOGGER.info("Initiating money transfer from {} to {} for amount {}.", sender, receiver, moneyToTransfer);

        var transaction = sessionProvider.get()
                .getTransaction();

        var executeSuccessfully = false;

        try {
            transaction.begin();
            executeTransfer(sender, receiver, moneyToTransfer);
            transaction.commit();

            executeSuccessfully = true;
            LOGGER.info("Money transfer completed.");
        } catch (RuntimeException e) {
            LOGGER.error("An error occurred while executing the transaction.", e);
            throw e;
        } finally {
            if (!executeSuccessfully) {
                transaction.rollback();
                LOGGER.error("Transaction was roll-backed.");
            }
        }
    }

    private void executeTransfer(AccountDTO sender, AccountDTO receiver, Money moneyToTransfer) {
        var senderEntity = accountsDAO.findById(sender.getId(), PESSIMISTIC_WRITE)
                .orElseThrow(() -> new AccountNotFoundException(sender.getId()));

        validateEnoughBalance(senderEntity, moneyToTransfer);

        var receiverEntity = accountsDAO.findById(receiver.getId(), PESSIMISTIC_WRITE)
                .orElseThrow(() -> new AccountNotFoundException(receiver.getId()));

        transferMoney(senderEntity, receiverEntity, moneyToTransfer);

        accountsDAO.update(senderEntity);
        accountsDAO.update(receiverEntity);

        logTransfer(senderEntity, receiverEntity, moneyToTransfer.getValue());
    }

    private void transferMoney(AccountEntity sender, AccountEntity receiver, Money moneyToTransfer) {
        var moneyToTransferValue = moneyToTransfer.getValue();

        var sendersNewBalance = sender.getBalance().subtract(moneyToTransferValue);
        var receiversNewBalance = receiver.getBalance().add(moneyToTransferValue);

        sender.setBalance(sendersNewBalance);
        receiver.setBalance(receiversNewBalance);
    }

    private void logTransfer(AccountEntity senderEntity, AccountEntity receiverEntity, BigDecimal amount) {
        transferLogDAO.save(
                new TransferLogEntity(
                        senderEntity,
                        receiverEntity,
                        amount
                )
        );
    }

    private void validateEnoughBalance(AccountEntity fromEntity, Money amountToTransfer) {
        if (fromEntity.getBalance().compareTo(amountToTransfer.getValue()) < 0) {
            throw new InSufficientEnoughBalanceException(fromEntity.getBalance(), amountToTransfer.getValue());
        }
    }

    private void checkValidArgs(AccountDTO sender, AccountDTO receiver, Money moneyToTransfer) {
        requireNonNull(sender);
        requireNonNull(receiver);
        requireNonNull(moneyToTransfer);

        if (moneyToTransfer.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Money to transfer should be greater than 0. Provided: " + moneyToTransfer.getValue());
        }
    }
}
