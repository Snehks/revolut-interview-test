package com.revolut.interview.account;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.interview.persistence.PersistenceModule;
import com.revolut.interview.rest.SparkRestModule;
import org.eclipse.jetty.http.HttpStatus;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import spark.Service;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountsResourceIntegrationTest {

    private static final int PORT = 8001;

    private Injector injector;

    @BeforeAll
    void setUp() {
        injector = Guice.createInjector(new AccountsModule(),
                new SparkRestModule(),
                new PersistenceModule()
        );

        var spark = injector.getInstance(Service.class);
        spark.port(PORT);

        var accountsResource = injector.getInstance(AccountsResource.class);
        accountsResource.register(spark);
    }

    @Test
    void getAccountShouldReturnEmptyBodyWhenAccountDoesNotExist() {
        given()
                .port(PORT)
                .get("/account/1")
                .then()
                .statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    void getAccountShouldReturnExpectedAccountJsonWhenAccountExists() {
        var accountsDAO = injector.getInstance(AccountsDAO.class);

        var savedAccount = accountsDAO.save(new AccountEntity(new BigDecimal(10)));

        var response = given()
                .port(PORT)
                .get("/account/" + savedAccount.getId());

        response.then().statusCode(HttpStatus.OK_200);

        var jsonPath = response.jsonPath();

        assertEquals(jsonPath.getLong("id"), savedAccount.getId());
        assertEquals(jsonPath.getDouble("balance.value"), 10.00);
    }

    @Test
    void addAccountShouldAddAnAccountOnDatabaseAndReturnAddedAccount() {
        var response = given()
                .port(PORT)
                .body(new Account(null, BigDecimal.TEN))
                .post("/account");

        response.then().statusCode(HttpStatus.OK_200);

        var jsonPath = response.jsonPath();
        assertEquals(jsonPath.getDouble("balance.value"), 10.00);

        var savedAccountId = jsonPath.getLong("id");

        var accountsDAO = injector.getInstance(AccountsDAO.class);
        assertTrue(accountsDAO.findById(savedAccountId).isPresent());
    }

    @Test
    void addAccountShouldReturnBadRequestWhenMoneyIsNegative() {
        var response = given()
                .port(PORT)
                .body("{\"id\":1,\"balance\":-1}") //because the java object does not allow creation with negative values.
                .post("/account");

        response.then().statusCode(HttpStatus.BAD_REQUEST_400);
    }

    @AfterEach
    void cleanupDatabase() {
        var sessionProvider = injector.getProvider(Session.class);

        var session = sessionProvider.get();

        var transaction = session.beginTransaction();
        session.createSQLQuery("DELETE from account").executeUpdate();
        transaction.commit();
    }

    @AfterAll
    void stopServer() {
        injector.getInstance(Service.class)
                .stop();
    }
}
