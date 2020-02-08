package com.revolut.interview.transfer;

import com.revolut.interview.account.Account;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionEntity;
import com.revolut.interview.transactions.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.revolut.interview.transactions.TransactionState.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Account SENDER = new Account(1L, BigDecimal.TEN);
    private static final Account RECEIVER = new Account(2L, BigDecimal.ONE);
    private static final BigDecimal MONEY_TO_TRANSFER = BigDecimal.valueOf(5);

    private static final TransferRequest VALID_TRANSFER_REQUEST = new TransferRequest(
            SENDER.getId(),
            RECEIVER.getId(),
            MONEY_TO_TRANSFER
    );

    @Mock
    private AccountsDAO accountsDAO;
    @Mock
    private TransactionDAO transactionDAO;
    @Mock
    private TransactionService transactionHandler;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                accountsDAO,
                transactionDAO,
                transactionHandler
        );

        setUpAccounts();
        setUpTransactionDAO();
    }

    @Test
    void shouldThrowAccountNotFoundExceptionIfSenderDoesNotExist() {
        var accountNotFoundException = assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(new TransferRequest(100L, RECEIVER.getId(), MONEY_TO_TRANSFER))
        );

        assertEquals(100L, accountNotFoundException.getId().longValue());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionIfReceiverDoesNotExist() {
        var accountNotFoundException = assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(new TransferRequest(SENDER.getId(), 100L, MONEY_TO_TRANSFER))
        );

        assertEquals(100L, accountNotFoundException.getId().longValue());
    }

    @Test
    void shouldThrowExceptionWhenMoneyToTransferIsLessThanEqualToZero() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(createTransferRequest(-1))
        );

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(createTransferRequest(0))
        );
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInSufficient() {
        assertThrows(InsufficientBalanceException.class,
                () -> transferService.transfer(createTransferRequest(200))
        );
    }

    @Test
    void shouldThrowExceptionWhenSenderAndReceiverAccountsAreSame() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(new TransferRequest(1L, 1L, BigDecimal.valueOf(10)))
        );
    }

    @Test
    void transactionIsCreatedAndSavedOnDatabaseWithExpectedParameters() {
        transferService.transfer(VALID_TRANSFER_REQUEST);

        var transactionEntityCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionDAO).save(transactionEntityCaptor.capture());

        var transactionEntity = transactionEntityCaptor.getValue();

        assertEquals(MONEY_TO_TRANSFER.compareTo(transactionEntity.getAmount()), 0);
        assertEquals(SENDER.getId(), transactionEntity.getSender().getId());
        assertEquals(RECEIVER.getId(), transactionEntity.getReceiver().getId());
        assertEquals(PENDING, transactionEntity.getTransactionState());
    }

    private void setUpAccounts() {
        var sender = new AccountEntity();
        sender.setId(SENDER.getId());
        sender.setBalance(SENDER.getBalance());

        var receiver = new AccountEntity();
        receiver.setId(RECEIVER.getId());
        receiver.setBalance(RECEIVER.getBalance());

        lenient()
                .when(accountsDAO.findById(SENDER.getId()))
                .thenReturn(Optional.of(sender));

        lenient()
                .when(accountsDAO.findById(RECEIVER.getId()))
                .thenReturn(Optional.of(receiver));
    }

    private void setUpTransactionDAO() {
        lenient()
                .when(transactionDAO.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    var transactionEntity = (TransactionEntity) invocation.getArgument(0);
                    transactionEntity.setId(1L);
                    return transactionEntity;
                });
    }

    private TransferRequest createTransferRequest(double amountToTransfer) {
        return new TransferRequest(SENDER.getId(), RECEIVER.getId(), BigDecimal.valueOf(amountToTransfer));
    }
}
