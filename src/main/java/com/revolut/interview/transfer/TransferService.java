package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountNotFoundException;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionEntity;
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
    private final TransactionDAO transactionDAO;

    @Inject
    TransferService(Provider<Session> sessionProvider, AccountsDAO accountsDAO, TransactionDAO transactionDAO) {
        this.sessionProvider = sessionProvider;
        this.accountsDAO = accountsDAO;
        this.transactionDAO = transactionDAO;
    }

    public Long transfer(TransferRequest transferRequestDTO) {
        checkValidArgs(transferRequestDTO);

        LOGGER.info("Initiating money transfer {}.", transferRequestDTO);

        var transaction = sessionProvider.get()
                .getTransaction();

        var executeSuccessfully = false;

        try {
            transaction.begin();
            var transactionId = executeTransfer(transferRequestDTO);
            transaction.commit();

            executeSuccessfully = true;
            LOGGER.info("Money transfer completed.");

            return transactionId;
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

    private Long executeTransfer(TransferRequest transferRequestDTO) {
        var senderId = transferRequestDTO.getSenderId();
        var receiverId = transferRequestDTO.getReceiverId();
        var moneyToTransfer = transferRequestDTO.getAmountToTransfer();

        var senderEntity = accountsDAO.findById(senderId, PESSIMISTIC_WRITE)
                .orElseThrow(() -> new AccountNotFoundException(senderId));

        validateEnoughBalance(senderEntity, moneyToTransfer.getValue());

        var receiverEntity = accountsDAO.findById(receiverId, PESSIMISTIC_WRITE)
                .orElseThrow(() -> new AccountNotFoundException(receiverId));

        transferMoney(senderEntity, receiverEntity, moneyToTransfer.getValue());

        accountsDAO.update(senderEntity);
        accountsDAO.update(receiverEntity);

        return logTransfer(senderEntity, receiverEntity, moneyToTransfer.getValue());
    }

    private void transferMoney(AccountEntity sender, AccountEntity receiver, BigDecimal moneyToTransfer) {
        var sendersNewBalance = sender.getBalance().subtract(moneyToTransfer);
        var receiversNewBalance = receiver.getBalance().add(moneyToTransfer);

        sender.setBalance(sendersNewBalance);
        receiver.setBalance(receiversNewBalance);
    }

    private Long logTransfer(AccountEntity senderEntity, AccountEntity receiverEntity, BigDecimal amount) {
        var transaction = transactionDAO.save(
                new TransactionEntity(
                        senderEntity,
                        receiverEntity,
                        amount
                )
        );

        return transaction.getId();
    }

    private void validateEnoughBalance(AccountEntity fromEntity, BigDecimal amountToTransfer) {
        if (fromEntity.getBalance().compareTo(amountToTransfer) < 0) {
            throw new InSufficientEnoughBalanceException(fromEntity.getBalance(), amountToTransfer);
        }
    }

    private void checkValidArgs(TransferRequest transferRequestDTO) {
        requireNonNull(transferRequestDTO);

        if (transferRequestDTO.getAmountToTransfer().getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Money to transfer should be greater than 0. Provided: " + transferRequestDTO.getAmountToTransfer());
        }
    }
}
