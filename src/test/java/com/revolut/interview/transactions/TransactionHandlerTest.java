package com.revolut.interview.transactions;

import com.revolut.interview.account.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    private TransactionHandler transactionHandler;

    @BeforeEach
    void setUp() {
        this.transactionHandler = new TransactionHandler(transactionExecutor, transactionDAO);
    }

    @Test
    void handleShouldThrowExceptionWhenTransactionIsNotFound() {
        when(transactionDAO.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> transactionHandler.queue(1L));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenStateIsNotPending() {
        var transactionEntity = mock(TransactionEntity.class);
        when(transactionEntity.getTransactionState()).thenReturn(TransactionState.SUCCEEDED);

        when(transactionDAO.findById(anyLong())).thenReturn(Optional.of(transactionEntity));

        assertThrows(IllegalArgumentException.class, () -> transactionHandler.queue(1L));
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

        transactionHandler.queue(transactionEntity.getId());

        var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionExecutor).execute(transactionCaptor.capture());

        var transaction = transactionCaptor.getValue();

        assertEquals(sender.getId().longValue(), transaction.getSenderId());
        assertEquals(receiver.getId().longValue(), transaction.getReceiverId());
        assertEquals(BigDecimal.ONE, transaction.getAmountToTransfer());
    }
}
