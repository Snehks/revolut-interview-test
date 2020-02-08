package com.revolut.interview.account;

import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpStatus;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountsResourceTest {

    @Mock
    private AccountsService accountsService;

    @Mock
    private Service spark;
    @Mock
    private Request request;
    @Mock
    private Response response;

    @Mock
    private Gson gson;

    private AccountsResource accountsResource;

    @BeforeEach
    void setUp() {
        this.accountsResource = new AccountsResource(accountsService, gson);
    }

    @Test
    void getAccountShouldReturnAccountWhenAccountIsPresent() throws Exception {
        var expectedAccount = Optional.of(mock(Account.class));
        when(accountsService.getById(anyLong())).thenReturn(expectedAccount);

        when(request.params("id")).thenReturn("1");

        accountsResource.register(spark);

        var routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(spark).get(eq("/api/account/:id"), routeCaptor.capture());

        var actualAccount = routeCaptor.getValue().handle(request, this.response);
        assertEquals(expectedAccount.get(), actualAccount);
    }

    @Test
    void getAccountShouldReturn404WithNullWhenAccountNotPresent() throws Exception {
        when(accountsService.getById(anyLong())).thenReturn(Optional.empty());
        when(request.params("id")).thenReturn("1");

        accountsResource.register(spark);

        var routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(spark).get(eq("/api/account/:id"), routeCaptor.capture());

        var accountResponse = routeCaptor.getValue().handle(request, response);

        verify(response).status(HttpStatus.NOT_FOUND_404);
        assertNull(accountResponse);
    }

    @Test
    void addAccountShouldCallAddAccountOnServiceAndReturnCreatedAccount() throws Exception {
        accountsResource.register(spark);
        when(request.body()).thenReturn("account_request_json");

        var accountToBeCreated = mock(Account.class);
        when(gson.fromJson(anyString(), eq(Account.class))).thenReturn(accountToBeCreated);

        when(accountsService.save(accountToBeCreated)).thenReturn(accountToBeCreated);

        var routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(spark).post(eq("/api/account"), routeCaptor.capture());

        var actualAccount = routeCaptor.getValue().handle(request, response);
        assertEquals(accountToBeCreated, actualAccount);
    }

    @Test
    void registerShouldRegisterEndpointsWithValidRoutes() {
        accountsResource.register(spark);

        verify(spark).post(eq("/api/account"), any(Route.class));
        verify(spark).get(eq("/api/account/:id"), any(Route.class));
    }
}
