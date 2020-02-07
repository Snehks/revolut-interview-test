package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountNotFoundException;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionEntity;
import com.revolut.interview.transactions.TransactionHandler;
import com.revolut.interview.transactions.TransactionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

@Singleton
public class TransferService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final AccountsDAO accountsDAO;
    private final TransactionDAO transactionDAO;

    private final TransactionHandler transactionHandler;

    @Inject
    TransferService(AccountsDAO accountsDAO, TransactionDAO transactionDAO, TransactionHandler transactionHandler) {
        this.accountsDAO = accountsDAO;
        this.transactionDAO = transactionDAO;
        this.transactionHandler = transactionHandler;
    }

    public void transfer(TransferRequest transferRequestDTO) {
        checkValidArgs(transferRequestDTO);

        LOGGER.info("Initiating money transfer {}.", transferRequestDTO);

        var transaction = createTransaction(transferRequestDTO);

        transactionHandler.queue(transaction.getId());

        /*var executeSuccessfully = false;

        try {
            transaction.begin();
            var transactionId = executeTransfer(transferRequestDTO);
            transaction.commit();

            executeSuccessfully = true;
            LOGGER.info("Money transfer completed.");

            return transactionId;
        } catch (PessimisticLockException e) {
            LOGGER.error("Transaction could not be completed because account was updated.", e);
            throw e;
        } finally {
            if (!executeSuccessfully) {
                transaction.rollback();
                LOGGER.error("Transaction was roll-backed.");
            }
        }*/
    }

    private TransactionEntity createTransaction(TransferRequest transferRequestDTO) {
        var senderId = transferRequestDTO.getSenderId();
        var receiverId = transferRequestDTO.getReceiverId();
        var moneyToTransfer = transferRequestDTO.getAmountToTransfer();

        var senderEntity = accountsDAO.findById(senderId)
                .orElseThrow(() -> new AccountNotFoundException(senderId));

        var receiverEntity = accountsDAO.findById(receiverId)
                .orElseThrow(() -> new AccountNotFoundException(receiverId));

        return transactionDAO.save(
                new TransactionEntity(
                        senderEntity,
                        receiverEntity,
                        moneyToTransfer.getValue(),
                        TransactionState.PENDING
                )
        );
    }

    /*private void transferMoney(AccountEntity sender, AccountEntity receiver, BigDecimal moneyToTransfer) {
        transferMoney(senderEntity, receiverEntity, moneyToTransfer.getValue());

        accountsDAO.update(senderEntity);
        accountsDAO.update(receiverEntity);

        var sendersNewBalance = sender.getBalance().subtract(moneyToTransfer);
        var receiversNewBalance = receiver.getBalance().add(moneyToTransfer);

        sender.setBalance(sendersNewBalance);
        receiver.setBalance(receiversNewBalance);
    }*/

   /* private void validateEnoughBalance(AccountEntity fromEntity, BigDecimal amountToTransfer) {
        if (fromEntity.getBalance().compareTo(amountToTransfer) < 0) {
            throw new InSufficientBalanceException(fromEntity.getBalance(), amountToTransfer);
        }
    }*/

    private void checkValidArgs(TransferRequest transferRequestDTO) {
        requireNonNull(transferRequestDTO);

        if (transferRequestDTO.getAmountToTransfer().isLessThanEqualToZero()) {
            throw new IllegalArgumentException("Money to transfer should be greater than 0. Provided: " + transferRequestDTO.getAmountToTransfer());
        }
    }
}
