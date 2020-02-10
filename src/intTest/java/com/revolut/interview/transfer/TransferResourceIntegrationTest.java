package com.revolut.interview.transfer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.interview.account.AccountEntity;
import com.revolut.interview.account.AccountsDAO;
import com.revolut.interview.account.AccountsModule;
import com.revolut.interview.notification.NotificationService;
import com.revolut.interview.notification.TransactionNotification;
import com.revolut.interview.persistence.PersistenceModule;
import com.revolut.interview.rest.SparkRestModule;
import com.revolut.interview.transactions.TransactionDAO;
import com.revolut.interview.transactions.TransactionModule;
import com.revolut.interview.transactions.TransactionState;
import org.eclipse.jetty.http.HttpStatus;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import spark.Service;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static javax.persistence.LockModeType.READ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferResourceIntegrationTest {

    private static final int PORT = 8001;
    private static final String BASE_PATH = "/api/transfer";

    private static final BigDecimal BALANCE = BigDecimal.TEN;

    private Injector injector;

    private AccountsDAO accountsDAO;
    private TransactionDAO transactionDAO;

    private AccountEntity sender;
    private AccountEntity receiver;

    @BeforeAll
    void initialise() {
        injector = Guice.createInjector(new AccountsModule(),
                new TransferModule(),
                new TransactionModule(),
                new SparkRestModule(),
                new PersistenceModule(),
                new TestNotificationModule()
        );

        var spark = injector.getInstance(Service.class);
        spark.port(PORT);

        var transferResource = injector.getInstance(TransferResource.class);
        transferResource.register(spark);

        this.accountsDAO = injector.getInstance(AccountsDAO.class);
        this.transactionDAO = injector.getInstance(TransactionDAO.class);
    }

    @BeforeEach
    void setUp() {
        createAccounts();

        reset(injector.getInstance(NotificationService.class)); //reset so to not carry state
    }

    @Test
    void transferShouldTransferMoneyFromSenderToReceiverWhenArgumentsProvidedAreValid() {
        var transferRequest = new TransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(5));
        var response = given()
                .port(PORT)
                .body(transferRequest)
                .post(BASE_PATH);

        response.then().statusCode(HttpStatus.OK_200);

        var sender = accountsDAO.findById(this.sender.getId(), READ)
                .orElseThrow();
        var receiver = accountsDAO.findById(this.receiver.getId(), READ)
                .orElseThrow();

        assertEquals(BALANCE.subtract(BigDecimal.valueOf(5)).compareTo(sender.getBalance()), 0);
        assertEquals(BALANCE.add(BigDecimal.valueOf(5)).compareTo(receiver.getBalance()), 0);

        var allTransactionsForSender = transactionDAO.findAllWithAccountId(sender.getId());
        assertEquals(1, allTransactionsForSender.size());

        var transaction = allTransactionsForSender.get(0);

        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(BigDecimal.valueOf(5).compareTo(transaction.getAmount()), 0);
        assertEquals(TransactionState.SUCCEEDED, transaction.getTransactionState());

        verifyNotification(transferRequest, true);
    }

    @Test
    void transferShouldReturnBadRequestResponseWhenSenderAndReceiverAreTheSame() {
        var response = given()
                .port(PORT)
                .body(new TransferRequest(sender.getId(), sender.getId(), BigDecimal.valueOf(5)))
                .post(BASE_PATH);

        response.then().statusCode(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    void transferShouldReturnBadRequestResponseWhenBalanceIsLowWhenMakingTheTransfer() {
        var transferRequest = new TransferRequest(sender.getId(), receiver.getId(), BigDecimal.valueOf(20));
        var response = given()
                .port(PORT)
                .body(transferRequest)
                .post(BASE_PATH);

        response.then().statusCode(HttpStatus.BAD_REQUEST_400);
    }

    private void verifyNotification(TransferRequest transferRequest, boolean success) {
        var notificationCaptor = ArgumentCaptor.forClass(TransactionNotification.class);
        var notificationService = injector.getInstance(NotificationService.class);
        verify(notificationService).sendNotification(notificationCaptor.capture());

        var notification = notificationCaptor.getValue();
        assertEquals(success, notification.isSuccess());
        assertEquals(transferRequest.getSenderId(), notification.getSenderId());
        assertEquals(transferRequest.getReceiverId(), notification.getReceiverId());
        assertEquals(0, transferRequest.getAmountToTransfer().compareTo(notification.getAmount()));
    }

    @Test
    void transferShouldReturnBadRequestWhenSenderAccountDoesNotExist() {
        var response = given()
                .port(PORT)
                .body(new TransferRequest(-1L, receiver.getId(), BigDecimal.valueOf(20)))
                .post(BASE_PATH);

        response.then().statusCode(HttpStatus.BAD_REQUEST_400);
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

    private void createAccounts() {
        this.sender = accountsDAO.save(new AccountEntity(BALANCE));
        this.receiver = accountsDAO.save(new AccountEntity(BALANCE));
    }

    private static class TestNotificationModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(NotificationService.class).toInstance(mock(NotificationService.class));
        }
    }
}
