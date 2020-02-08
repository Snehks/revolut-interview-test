package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionEntity;
import com.revolut.interview.transactions.TransactionService;
import com.revolut.interview.transactions.TransactionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Singleton
class TransferService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final AccountsDAO accountsDAO;
    private final TransactionDAO transactionDAO;

    private final TransactionService transactionHandler;

    @Inject
    TransferService(AccountsDAO accountsDAO, TransactionDAO transactionDAO, TransactionService transactionHandler) {
        this.accountsDAO = accountsDAO;
        this.transactionDAO = transactionDAO;
        this.transactionHandler = transactionHandler;
    }

    public void transfer(TransferRequest transferRequestDTO) {
        checkValidArgs(transferRequestDTO);

        LOGGER.info("Initiating money transfer {}.", transferRequestDTO);

        var transaction = createTransaction(transferRequestDTO);

        transactionHandler.queue(transaction.getId());
    }

    private TransactionEntity createTransaction(TransferRequest transferRequestDTO) {
        var senderId = transferRequestDTO.getSenderId();
        var receiverId = transferRequestDTO.getReceiverId();
        var moneyToTransfer = transferRequestDTO.getAmountToTransfer();

        var senderEntity = accountsDAO.findById(senderId)
                .orElseThrow(() -> new AccountNotFoundException(senderId));

        var receiverEntity = accountsDAO.findById(receiverId)
                .orElseThrow(() -> new AccountNotFoundException(receiverId));

        if (senderEntity.getBalance().compareTo(transferRequestDTO.getAmountToTransfer()) < 0) {
            throw new InsufficientBalanceException(senderEntity.getBalance(), transferRequestDTO.getAmountToTransfer());
        }

        return transactionDAO.save(
                new TransactionEntity(
                        senderEntity,
                        receiverEntity,
                        moneyToTransfer,
                        TransactionState.PENDING
                )
        );
    }

    private void checkValidArgs(TransferRequest transferRequestDTO) {
        requireNonNull(transferRequestDTO);

        if (transferRequestDTO.getAmountToTransfer().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Money to transfer should be greater than 0. Provided: " + transferRequestDTO.getAmountToTransfer());
        }

        if (transferRequestDTO.getSenderId() == transferRequestDTO.getReceiverId()) {
            throw new IllegalArgumentException("Receiver and Sender accounts cannot be the same.");
        }
    }
}
