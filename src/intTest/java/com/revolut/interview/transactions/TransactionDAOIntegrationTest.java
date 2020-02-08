package com.revolut.interview.transactions;

import com.google.inject.Guice;
import com.google.inject.Provider;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.persistence.PersistenceModule;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionDAOIntegrationTest {

    private TransactionDAO transactionDAO;
    private Provider<Session> sessionProvider;

    private AccountEntity sender;
    private AccountEntity receiver;
    private AccountsDAO accountsDAO;

    @BeforeEach
    void setUp() {
        var injector = Guice.createInjector(new PersistenceModule());

        this.transactionDAO = injector.getInstance(TransactionDAO.class);
        this.accountsDAO = injector.getInstance(AccountsDAO.class);
        this.sessionProvider = injector.getProvider(Session.class);

        setUpAccounts();
    }

    @Test
    void findByIdShouldReturnExpectedEntity() {
        var toSave = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        var savedEntity = transactionDAO.save(toSave);

        var savedEntityById = transactionDAO.findById(savedEntity.getId())
                .orElseThrow();

        assertEquals(toSave, savedEntityById);
    }

    @Test
    void findByIdShouldReturnExpectedEntityWhenTransactionIsRunningOutside() {
        var toSave = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);

        var session = sessionProvider.get();
        session.getTransaction().begin();

        var savedEntity = transactionDAO.save(toSave);
        var accountEntityById = transactionDAO.findById(savedEntity.getId())
                .orElseThrow();

        session.getTransaction().commit();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void updateStateShouldPersistUpdatedState() {
        var toSave = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        var savedEntity = transactionDAO.save(toSave);

        var result = transactionDAO.updateState(savedEntity.getId(), TransactionState.PENDING, TransactionState.IN_PROGRESS);
        assertTrue(result);

        var updatedEntityById = transactionDAO.findById(savedEntity.getId())
                .orElseThrow();

        assertEquals(TransactionState.IN_PROGRESS, updatedEntityById.getTransactionState());
    }

    @Test
    void updateStateShouldNotUpdateStateWhenCurrentStateMismatches() {
        var toSave = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        var savedEntity = transactionDAO.save(toSave);

        var result = transactionDAO.updateState(savedEntity.getId(), TransactionState.IN_PROGRESS, TransactionState.SUCCEEDED);
        assertFalse(result);

        var entityById = transactionDAO.findById(savedEntity.getId())
                .orElseThrow();

        assertEquals(TransactionState.PENDING, entityById.getTransactionState());
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoTransactionForAccountId() {
        var toSave = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        transactionDAO.save(toSave);

        var allTransactions = transactionDAO.findAllWithAccountId(4L);

        assertTrue(allTransactions.isEmpty());
    }

    @Test
    void findAllShouldReturnOnlyTheTransactionsForGivenAccountId() {
        var transaction1 = new TransactionEntity(sender, receiver, BigDecimal.ONE, TransactionState.PENDING);
        var transaction2 = new TransactionEntity(receiver, sender, BigDecimal.ONE, TransactionState.PENDING);
        var transaction3 = new TransactionEntity(accountsDAO.save(new AccountEntity(BigDecimal.TEN)), receiver, BigDecimal.ONE, TransactionState.PENDING);

        var savedTransaction1 = transactionDAO.save(transaction1);
        var savedTransaction2 = transactionDAO.save(transaction2);
        transactionDAO.save(transaction3);

        var allTransactions = transactionDAO.findAllWithAccountId(sender.getId());

        assertEquals(allTransactions.size(), 2);

        assertTrue(allTransactions.contains(savedTransaction1));
        assertTrue(allTransactions.contains(savedTransaction2));
    }

    private void setUpAccounts() {
        sender = accountsDAO.save(new AccountEntity(BigDecimal.ONE));
        receiver = accountsDAO.save(new AccountEntity(BigDecimal.TEN));
    }
}
