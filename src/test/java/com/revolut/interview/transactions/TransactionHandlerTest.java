package com.revolut.interview.transactions;

import com.revolut.interview.account.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHandlerTest {

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private TransactionDAO transactionDAO;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        this.transactionService = new TransactionService(transactionExecutor, transactionDAO);
    }

    @Test
    void handleShouldThrowExceptionWhenTransactionIsNotFound() {
        when(transactionDAO.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionException.class, () -> transactionService.queue(1L));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenStateIsNotPending() {
        var transactionEntity = mock(TransactionEntity.class);
        when(transactionEntity.getTransactionState()).thenReturn(TransactionState.SUCCEEDED);

        when(transactionDAO.findById(anyLong())).thenReturn(Optional.of(transactionEntity));

        assertThrows(InvalidTransactionException.class, () -> transactionService.queue(1L));
    }

    @Test
    void transactionWithExpectedArgsShouldBeSentForExecutionWhenTransactionIsFound() {
        var sender = mock(AccountEntity.class);
        var receiver = mock(AccountEntity.class);

        when(sender.getId()).thenReturn(1L);
        when(receiver.getId()).thenReturn(2L);

        var transactionEntity = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        transactionEntity.setId(1L);
        when(transactionDAO.findById(transactionEntity.getId())).thenReturn(Optional.of(transactionEntity));

        transactionService.queue(transactionEntity.getId());

        var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionExecutor).execute(transactionCaptor.capture());

        var transaction = transactionCaptor.getValue();

        assertEquals(sender.getId(), transaction.getSenderId());
        assertEquals(receiver.getId(), transaction.getReceiverId());
        assertEquals(BigDecimal.ONE, transaction.getAmountToTransfer());
        assertEquals(transactionEntity.getTransactionState(), transaction.getTransactionState());
    }

    @Test
    void getAllTransactionsShouldGetAllTheTransactionsFromDaoMapped() {
        var sender = mock(AccountEntity.class);
        var receiver = mock(AccountEntity.class);

        when(sender.getId()).thenReturn(1L);
        when(receiver.getId()).thenReturn(2L);

        var transactionEntity1 = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        transactionEntity1.setId(1L);

        var transactionEntity2 = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        transactionEntity2.setId(2L);

        when(transactionDAO.findAllWithAccountId(1L)).thenReturn(List.of(transactionEntity1, transactionEntity2));

        var allTransactionsForAccountId = transactionService.getAllTransactionsForAccountId(1L);

        assertEquals(2, allTransactionsForAccountId.size());

        hasTransaction(transactionEntity1, allTransactionsForAccountId);
        hasTransaction(transactionEntity2, allTransactionsForAccountId);
    }

    @Test
    void getAllTransactionsShouldReturnEmptyListWhenDAOReturnsEmptyList() {
        when(transactionDAO.findAllWithAccountId(1L)).thenReturn(List.of());

        var allTransactionsForAccountId = transactionService.getAllTransactionsForAccountId(1L);

        assertEquals(0, allTransactionsForAccountId.size());
    }

    private void hasTransaction(TransactionEntity transactionEntity, List<Transaction> allTransactions) {
        var transaction = allTransactions.stream()
                .filter(t -> t.getTransactionId() == transactionEntity.getId())
                .findFirst()
                .orElseThrow();

        assertEquals(0, transactionEntity.getAmount().compareTo(transaction.getAmountToTransfer()));
        assertEquals(transactionEntity.getReceiver().getId(), transaction.getReceiverId());
        assertEquals(transactionEntity.getSender().getId(), transaction.getSenderId());
        assertEquals(transactionEntity.getTransactionState(), transaction.getTransactionState());
    }
}
