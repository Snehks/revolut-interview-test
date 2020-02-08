package com.revolut.interview.transactions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.notification.NotificationsModule;
import com.revolut.interview.persistence.PersistenceModule;
import com.revolut.interview.rest.SparkRestModule;
import io.restassured.common.mapper.TypeRef;
import org.eclipse.jetty.http.HttpStatus;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import spark.Service;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionResourceIntegrationTest {

    private static final String BASE_PATH = "/api/transactions";

    private static final int PORT = 8001;

    private Injector injector;

    @BeforeAll
    void setUp() {
        injector = Guice.createInjector(new TransactionModule(),
                new SparkRestModule(),
                new PersistenceModule(),
                new NotificationsModule()
        );

        var spark = injector.getInstance(Service.class);
        spark.port(PORT);

        var transactionResource = injector.getInstance(TransactionResource.class);
        transactionResource.register(spark);
    }

    @Test
    void getTransactionsShouldReturnEmptyJsonWhenNoTransactionExists() {
        given()
                .port(PORT)
                .get(BASE_PATH + "/1")
                .then()
                .body(equalTo("[]"))
                .statusCode(HttpStatus.OK_200);
    }

    @Test
    void getTransactionShouldReturnExpectedTransactionJsonForGivenAccount() {
        var accountsDAO = injector.getInstance(AccountsDAO.class);
        var account1 = accountsDAO.save(new AccountEntity(new BigDecimal(10)));
        var account2 = accountsDAO.save(new AccountEntity(new BigDecimal(10)));
        var account3 = accountsDAO.save(new AccountEntity(new BigDecimal(10)));
        var account4 = accountsDAO.save(new AccountEntity(new BigDecimal(10)));

        var transactionDAO = injector.getInstance(TransactionDAO.class);
        var transaction1 = transactionDAO.save(new TransactionEntity(account1, account2, BigDecimal.TEN, TransactionState.SUCCEEDED));
        var transaction2 = transactionDAO.save(new TransactionEntity(account2, account1, BigDecimal.ONE, TransactionState.FAILED));
        var transaction3 = transactionDAO.save(new TransactionEntity(account1, account3, BigDecimal.ZERO, TransactionState.SUCCEEDED));
        var transaction4 = transactionDAO.save(new TransactionEntity(account3, account4, BigDecimal.TEN, TransactionState.SUCCEEDED));

        var response = given()
                .port(PORT)
                .get(BASE_PATH + "/" + account1.getId());

        response.then().statusCode(HttpStatus.OK_200);

        var transactionResponse = response.getBody().as(new TypeRef<List<Transaction>>() {
        });

        assertEquals(3, transactionResponse.size());

        verifyContainsTransaction(transaction1, transactionResponse);
        verifyContainsTransaction(transaction2, transactionResponse);
        verifyContainsTransaction(transaction3, transactionResponse);
    }

    private void verifyContainsTransaction(TransactionEntity transactionEntity, List<Transaction> transactionResponse) {
        var transaction = transactionResponse.stream()
                .filter(t -> t.getTransactionId() == transactionEntity.getId())
                .findFirst()
                .orElseThrow();

        assertEquals(transactionEntity.getSender().getId(), transaction.getSenderId());
        assertEquals(transactionEntity.getReceiver().getId(), transaction.getReceiverId());
        assertEquals(transactionEntity.getAmount().compareTo(transaction.getAmountToTransfer()), 0);
        assertEquals(transactionEntity.getTransactionState(), transaction.getTransactionState());
    }

    @AfterEach
    void cleanupDatabase() {
        var sessionProvider = injector.getProvider(Session.class);

        var session = sessionProvider.get();

        var transaction = session.beginTransaction();
        session.createSQLQuery("DELETE from transactions").executeUpdate();
        session.createSQLQuery("DELETE from account").executeUpdate();
        transaction.commit();
    }

    @AfterAll
    void stopServer() {
        injector.getInstance(Service.class)
                .stop();
    }
}
