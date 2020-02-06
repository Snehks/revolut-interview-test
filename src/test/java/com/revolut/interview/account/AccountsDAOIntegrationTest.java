package com.revolut.interview.account;

import com.google.inject.Guice;
import com.google.inject.Provider;
import com.revolut.interview.persistence.PersistenceModule;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.PessimisticLockException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;
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
        var accountEntityById = accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
                .orElseThrow();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void findByIdWithLockModeShouldReturnExpectedEntityWhenTransactionIsRunningOutside() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var session = sessionProvider.get();
        session.getTransaction().begin();

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
                .orElseThrow();

        session.getTransaction().commit();

        assertEquals(toSave, accountEntityById);
    }

    @Test
    void updateShouldUpdateEntity() {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
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

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
                .orElseThrow();

        accountEntityById.setBalance(BigDecimal.ZERO);
        accountsDAO.update(accountEntityById);

        session.getTransaction().commit();

        var updatedFindById = accountsDAO.findById(savedEntity.getId())
                .orElseThrow();
        assertEquals(accountEntityById, updatedFindById);
    }

    @Test
    void shouldThrowExceptionWhenMultipleTransactionsTryToUpdateAndAccessSameAccount() throws InterruptedException, ExecutionException {
        var toSave = new AccountEntity(BigDecimal.valueOf(10.3));
        var savedEntity = accountsDAO.save(toSave);

        var executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            var session = sessionProvider.get();
            session.getTransaction().begin();

            accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
                    .orElseThrow();

            var anotherUpdateFuture = executorService.submit(() -> updateBalance(savedEntity));
            var executionException = assertThrows(ExecutionException.class, anotherUpdateFuture::get);
            assertTrue(executionException.getCause() instanceof PessimisticLockException);

            session.getTransaction().commit();
        }).get();

        //Hate this hack but I am not familiar with awaitility testing framework so I will avoid it
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    void shouldNotThrowAnyExceptionWhenMultipleTransactionsTryToUpdateAndAccessDifferentAccounts() throws InterruptedException, ExecutionException {
        var toSave1 = new AccountEntity(BigDecimal.valueOf(10.3));
        var toSave2 = new AccountEntity(BigDecimal.valueOf(10.3));

        var savedEntity1 = accountsDAO.save(toSave1);
        var savedEntity2 = accountsDAO.save(toSave2);

        var executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            var session = sessionProvider.get();
            session.getTransaction().begin();

            var accountEntityById = accountsDAO.findById(savedEntity1.getId(), PESSIMISTIC_WRITE)
                    .orElseThrow();

            try {
                executorService.submit(() -> updateBalance(savedEntity2)).get();
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

    private void updateBalance(AccountEntity savedEntity) {
        var session = sessionProvider.get();
        session.getTransaction().begin();

        var accountEntityById = accountsDAO.findById(savedEntity.getId(), PESSIMISTIC_WRITE)
                .orElseThrow();

        accountEntityById.setBalance(BigDecimal.ZERO);
        accountsDAO.update(accountEntityById);

        session.getTransaction().commit();
    }
}
