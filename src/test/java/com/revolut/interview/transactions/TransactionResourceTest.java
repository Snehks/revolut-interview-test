package com.revolut.interview.transactions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionResourceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private Service spark;
    @Mock
    private Request request;
    @Mock
    private Response response;

    private TransactionResource transactionResource;

    @BeforeEach
    void setUp() {
        this.transactionResource = new TransactionResource(transactionService);
    }

    @Test
    void getTransactionsShouldReturnAllTransactionsForAccountId() throws Exception {
        var transaction1 = mock(Transaction.class);
        var transaction2 = mock(Transaction.class);

        when(transactionService.getAllTransactionsForAccountId(1L)).thenReturn(List.of(transaction1, transaction2));

        when(request.params("accountId")).thenReturn("1");

        transactionResource.register(spark);

        var routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(spark).get(eq("/transactions/:accountId"), routeCaptor.capture());

        @SuppressWarnings("unchecked")
        var allTransactions = (List<Transaction>) routeCaptor.getValue().handle(request, this.response);

        assertEquals(2, allTransactions.size());
        assertTrue(allTransactions.contains(transaction1));
        assertTrue(allTransactions.contains(transaction2));
    }

    @Test
    void registerShouldRegisterAllExpectedRoutes() {
        transactionResource.register(spark);

        verify(spark).get(eq("/transactions/:accountId"), any(Route.class));
    }
}
