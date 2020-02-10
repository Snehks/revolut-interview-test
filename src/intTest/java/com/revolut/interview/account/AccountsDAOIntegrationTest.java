package com.revolut.interview.account;

import com.google.inject.Guice;
import com.google.inject.Provider;
import com.revolut.interview.persistence.PersistenceModule;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static javax.persistence.LockModeType.READ;
import static javax.persistence.LockModeType.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AccountsDAOIntegrationTest {

    private AccountsDAO accountsDAO;
    private Provider<Session> sessionProvider;

    @BeforeEach
    void setUp() {
        var injector = Guice.createInjector(new PersistenceModule());

        this.accountsDAO = injector.getInstance(AccountsDAO.class);
        this.sessionProvider = injector.getProvider(Session.class);
    }

    @Test
    void findByIdShouldReturnExpectedEntity() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var accountEntityById = accountsDAO.findById(savedEntity.getId())
                .orElseThrow();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void findByIdShouldReturnExpectedEntityWhenTransactionIsRunningOutside() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));

        var session = sessionProvider.get();
        session.getTransaction().begin();

        var savedEntity = accountsDAO.save(toSave);
        var accountEntityById = accountsDAO.findById(savedEntity.getId())
                .orElseThrow();

        session.getTransaction().commit();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void findByIdWithLockModeShouldReturnExpectedEntity() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));

        var savedEntity = accountsDAO.save(toSave);
        var accountEntityById = accountsDAO.findById(savedEntity.getId(), READ)
                .orElseThrow();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void findByIdWithLockModeShouldReturnExpectedEntityWhenTransactionIsRunningOutside() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var session = sessionProvider.get();
        session.getTransaction().begin();

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), READ)
                .orElseThrow();

        session.getTransaction().commit();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void updateShouldUpdateEntity() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), WRITE)
                .orElseThrow();

        accountEntityById.setBalance(BigDecimal.ZERO);
        accountsDAO.update(accountEntityById);

        var updatedFindById = accountsDAO.findById(savedEntity.getId())
                .orElseThrow();
        assertEquals(accountEntityById, updatedFindById);
    }

    @Test
    void updateSuccessfullyWhenTransactionIsRunningOutside() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var session = sessionProvider.get();
        session.getTransaction().begin();

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), WRITE)
                .orElseThrow();

        accountEntityById.setBalance(BigDecimal.ZERO);
        accountsDAO.update(accountEntityById);

        session.getTransaction().commit();

        var updatedFindById = accountsDAO.findById(savedEntity.getId())
                .orElseThrow();
        assertEquals(accountEntityById, updatedFindById);
    }

    @Test
    void shouldThrowExceptionWhenMultipleTransactionsTryToUpdateAndAccessSameAccount() throws InterruptedException {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntityId = accountsDAO.save(toSave)
                .getId();

        var executorService = Executors.newFixedThreadPool(2);

        var updateFuture = executorService.submit(() -> {
            var session = sessionProvider.get();
            session.getTransaction().begin();

            var accountEntityById = accountsDAO.findById(savedEntityId, WRITE)
                    .orElseThrow();

            var anotherUpdateFuture = executorService.submit(() -> updateBalance(savedEntityId));
            try {
                anotherUpdateFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                fail("Test failed due to ", e);
            }

            accountEntityById.setBalance(BigDecimal.ZERO);
            accountsDAO.update(accountEntityById);

            session.getTransaction().commit();
        });

        var executionException = assertThrows(ExecutionException.class, updateFuture::get);
        assertTrue(executionException.getCause() instanceof OptimisticLockException);

        //Hate this hack but I am not familiar with awaitility testing framework so I will avoid it
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    void shouldNotThrowAnyExceptionWhenMultipleTransactionsTryToUpdateAndAccessDifferentAccounts() throws InterruptedException, ExecutionException {
        var toSave1 = new AccountEntity(BigDecimal.valueOf(10.3));
        var toSave2 = new AccountEntity(BigDecimal.valueOf(10.3));

        var savedEntity1Id = accountsDAO.save(toSave1).getId();
        var savedEntity2Id = accountsDAO.save(toSave2).getId();

        var executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            var session = sessionProvider.get();
            session.getTransaction().begin();

            var accountEntityById = accountsDAO.findById(savedEntity1Id, WRITE)
                    .orElseThrow();

            try {
                executorService.submit(() -> updateBalance(savedEntity2Id)).get();
            } catch (InterruptedException | ExecutionException e) {
                fail("Test failed due to error ", e);
            }

            accountEntityById.setBalance(BigDecimal.ZERO);
            accountsDAO.update(accountEntityById);

            session.getTransaction().commit();
        }).get();

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    //This behaviour is intentional as we don't want reading of accounts to fail when money is deposited to the bank account.
    @Test
    void accountShouldBeReadWithoutExceptionsEvenWhenMultipleTransactionsAreUpdatingTheSameAccount() throws InterruptedException {
        var accountEntityToSave = new AccountEntity(BigDecimal.valueOf(1));
        var savedAccountEntityId = accountsDAO.save(accountEntityToSave).getId();

        var executorService = Executors.newFixedThreadPool(2);

        executorService.execute(() -> {
            var updatesCount = 100;

            executorService.submit(() -> {
                for (int i = 0; i < updatesCount; i++) {
                    updateBalance(savedAccountEntityId);
                }
            });

            for (int i = 0; i < updatesCount; i++) {
                accountsDAO.findById(savedAccountEntityId);
            }
        });

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    private void updateBalance(long entityId) {
        var session = sessionProvider.get();
        session.getTransaction().begin();

        var accountEntityById = accountsDAO.findById(entityId, WRITE)
                .orElseThrow();

        accountEntityById.setBalance(BigDecimal.valueOf(Math.random()));
        accountsDAO.update(accountEntityById);

        session.getTransaction().commit();
    }
}
