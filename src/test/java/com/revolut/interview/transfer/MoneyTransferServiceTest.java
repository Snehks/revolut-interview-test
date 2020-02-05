package com.revolut.interview.transfer;

import com.revolut.interview.account.AccountDTO;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountNotFoundException;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.money.Money;
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

    private static final AccountDTO SENDER = new AccountDTO(1L, Money.valueOf(BigDecimal.TEN));
    private static final AccountDTO RECEIVER = new AccountDTO(2L, Money.valueOf(BigDecimal.ONE));
    private static final Money MONEY_TO_TRANSFER = Money.valueOf(5);

    @Mock
    private Session session;
    @Mock
    private Transaction transaction;
    @Mock
    private Provider<Session> sessionProvider;

    @Mock
    private AccountsDAO accountsDAO;

    @Mock
    private TransferLogDAO transferLogDAO;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(sessionProvider,
                accountsDAO,
                transferLogDAO
        );

        setUpSessionsAndTransactions();
        setUpAccounts();
    }

    @Test
    void shouldThrowAccountNotFoundExceptionIfSenderDoesNotExist() {
        var accountNotFoundException = assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(new AccountDTO(100L, Money.valueOf(1)), RECEIVER, Money.valueOf(100))
        );

        assertEquals(100L, accountNotFoundException.getId().longValue());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionIfReceiverDoesNotExist() {
        setUpSessionsAndTransactions();
        setUpAccounts();

        var accountNotFoundException = assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(SENDER, new AccountDTO(100L, Money.valueOf(1)), Money.valueOf(1))
        );

        assertEquals(100L, accountNotFoundException.getId().longValue());
    }

    @Test
    void transferShouldThrowExceptionWhenBalanceIsInsufficient() {
        assertThrows(InSufficientEnoughBalanceException.class,
                () -> transferService.transfer(SENDER, RECEIVER, Money.valueOf(100))
        );
    }

    @Test
    void shouldThrowExceptionWhenMoneyToTransferIsLessThanEqualToZero() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(SENDER, RECEIVER, Money.valueOf(-1))
        );

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(SENDER, RECEIVER, Money.valueOf(0))
        );
    }

    @Test
    void senderAccountShouldBeUpdatedWithExpectedParameters() {
        transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER);

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
        transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER);

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
        transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER);

        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileUpdatingSenderAccount() {
        simulateUpdateFailureForAccount(SENDER);

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER)
        );

        verify(transaction).rollback();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileUpdatingReceiverAccount() {
        simulateUpdateFailureForAccount(RECEIVER);

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER)
        );

        verify(transaction).rollback();
    }

    @Test
    void transactionShouldBeRolledBackIfAnExceptionIsThrownWhileSavingTransferLog() {
        doThrow(RuntimeException.class)
                .when(transferLogDAO)
                .save(any(TransferLogEntity.class));

        assertThrows(RuntimeException.class,
                () -> transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER)
        );

        verify(transaction).rollback();
    }

    @Test
    void moneyTransferIsLoggedOnDatabaseWithExpectedValues() {
        transferService.transfer(SENDER, RECEIVER, MONEY_TO_TRANSFER);

        var transferLogCaptor = ArgumentCaptor.forClass(TransferLogEntity.class);
        verify(transferLogDAO).save(transferLogCaptor.capture());

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

    private void simulateUpdateFailureForAccount(AccountDTO accountDTO) {
        doThrow(RuntimeException.class)
                .when(accountsDAO)
                .update(argThat(argument -> argument.getId().equals(accountDTO.getId())));

        lenient()
                .doNothing()
                .when(accountsDAO)
                .update(argThat(argument -> !argument.getId().equals(accountDTO.getId())));
    }

    private void setUpSessionsAndTransactions() {
        lenient()
                .when(sessionProvider.get()).thenReturn(session);

        lenient()
                .when(session.getTransaction()).thenReturn(transaction);
    }
}
