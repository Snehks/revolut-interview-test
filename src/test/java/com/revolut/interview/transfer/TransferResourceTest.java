package com.revolut.interview.transfer;

import com.google.gson.Gson;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferResourceTest {

    private static final String BASE_PATH = "/api/transfer";
    @Mock
    private TransferService transferService;

    @Mock
    private Service spark;
    @Mock
    private Request request;
    @Mock
    private Response response;

    @Mock
    private Gson gson;

    private TransferResource transferResource;

    @BeforeEach
    void setUp() {
        this.transferResource = new TransferResource(transferService, gson);
    }

    @Test
    void transferShouldSendTransferRequestToTransferService() throws Exception {
        var transferRequest = mock(TransferRequest.class);
        when(gson.fromJson(anyString(), eq(TransferRequest.class))).thenReturn(transferRequest);

        when(request.body()).thenReturn("transfer_json");

        transferResource.register(spark);

        var routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(spark).post(eq(BASE_PATH), routeCaptor.capture());

        routeCaptor.getValue().handle(request, response);
        verify(transferService).transfer(transferRequest);
    }

    @Test
    void registerShouldRegisterEndpointsWithValidRoutes() {
        transferResource.register(spark);

        verify(spark).post(eq(BASE_PATH), any(Route.class));
    }
}
