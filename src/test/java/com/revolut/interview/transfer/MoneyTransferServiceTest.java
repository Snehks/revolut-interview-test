package com.revolut.interview.transfer;

import com.revolut.interview.account.Account;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountNotFoundException;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.money.Money;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Optional;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MoneyTransferServiceTest {

    private static final Account SENDER = new Account(1L, Money.valueOf(BigDecimal.TEN));
    private static final Account RECEIVER = new Account(2L, Money.valueOf(BigDecimal.ONE));
    private static final Money MONEY_TO_TRANSFER = Money.valueOf(5);

    private static final TransferRequest VALID_TRANSFER_REQUEST = new TransferRequest(
            SENDER.getId(),
            RECEIVER.getId(),
            MONEY_TO_TRANSFER
    );

    @Mock
    private Session session;
    @Mock
    private Transaction transaction;
    @Mock
    private Provider<Session> sessionProvider;

    @Mock
    private AccountsDAO accountsDAO;
    @Mock
    private TransactionDAO transactionDAO;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(sessionProvider,
                accountsDAO,
                transactionDAO
        );

        setUpSessions();
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
    void transferShouldThrowExceptionWhenBalanceIsInsufficient() {
        assertThrows(InSufficientEnoughBalanceException.class,
                () -> transferService.transfer(createTransferRequest(100))
        );
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
    void senderAccountShouldBeUpdatedWithExpectedParameters() {
        transferService.transfer(VALID_TRANSFER_REQUEST);

        var accountEntityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountsDAO, times(2)).update(accountEntityCaptor.capture());

        var updatedEntities = accountEntityCaptor.getAllValues();
        var senderAccountEntity = updatedEntities.stream()
                .filter(e -> e.getId().equals(SENDER.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(SENDER.getBalance().subtract(MONEY_TO_TRANSFER).getValue(), senderAccountEntity.getBalance());
    }

    @Test
    void receiverAccountShouldBeUpdatedWithExpectedParameters() {
        transferService.transfer(VALID_TRANSFER_REQUEST);

        var accountEntityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountsDAO, times(2)).update(accountEntityCaptor.capture());

        var updatedEntities = accountEntityCaptor.getAllValues();
        var receiverAccountEntity = updatedEntities.stream()
                .filter(e -> e.getId().equals(RECEIVER.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(RECEIVER.getBalance().add(MONEY_TO_TRANSFER).getValue(), receiverAccountEntity.getBalance());
    }

    @Test
    void databaseTransactionShouldBeCommittedWhenTransferSucceeds() {
        transferService.transfer(VALID_TRANSFER_REQUEST);

        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileUpdatingSenderAccount() {
        simulateUpdateFailureForAccount(SENDER);

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(VALID_TRANSFER_REQUEST)
        );

        verify(transaction).rollback();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileUpdatingReceiverAccount() {
        simulateUpdateFailureForAccount(RECEIVER);

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(VALID_TRANSFER_REQUEST)
        );

        verify(transaction).rollback();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileSavingTransferLog() {
        doThrow(RuntimeException.class)
                .when(transactionDAO)
                .save(any(TransactionEntity.class));

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(VALID_TRANSFER_REQUEST)
        );

        verify(transaction).rollback();
    }

    @Test
    void moneyTransferIsLoggedOnDatabaseWithExpectedValues() {
        transferService.transfer(VALID_TRANSFER_REQUEST);

        var transferLogCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionDAO).save(transferLogCaptor.capture());

        var transferLogEntity = transferLogCaptor.getValue();

        assertEquals(MONEY_TO_TRANSFER.getValue(), transferLogEntity.getAmount());
        assertEquals(SENDER.getId(), transferLogEntity.getSender().getId());
        assertEquals(RECEIVER.getId(), transferLogEntity.getReceiver().getId());
    }

    private void setUpAccounts() {
        var sender = new AccountEntity();
        sender.setId(SENDER.getId());
        sender.setBalance(SENDER.getBalance().getValue());

        var receiver = new AccountEntity();
        receiver.setId(RECEIVER.getId());
        receiver.setBalance(RECEIVER.getBalance().getValue());

        lenient()
                .when(accountsDAO.findById(SENDER.getId(), PESSIMISTIC_WRITE))
                .thenReturn(Optional.of(sender));

        lenient()
                .when(accountsDAO.findById(RECEIVER.getId(), PESSIMISTIC_WRITE))
                .thenReturn(Optional.of(receiver));
    }

    private void simulateUpdateFailureForAccount(Account accountDTO) {
        doThrow(RuntimeException.class)
                .when(accountsDAO)
                .update(argThat(argument -> argument.getId().equals(accountDTO.getId())));

        lenient()
                .doNothing()
                .when(accountsDAO)
                .update(argThat(argument -> !argument.getId().equals(accountDTO.getId())));
    }

    private void setUpSessions() {
        lenient()
                .when(sessionProvider.get()).thenReturn(session);

        lenient()
                .when(session.getTransaction()).thenReturn(transaction);
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
        return new TransferRequest(SENDER.getId(), RECEIVER.getId(), Money.valueOf(amountToTransfer));
    }
}
