package com.revolut.interview.account;

import com.revolut.interview.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AccountsDAO accountsDAO;

    private AccountsService accountsService;

    @BeforeEach
    void setUp() {
        this.accountsService = new AccountsService(accountsDAO);
    }

    @Test
    void getByIdShouldReturnExpectedAccount() {
        var toReturnAfterSaveFromDAO = new AccountEntity(BigDecimal.TEN);
        toReturnAfterSaveFromDAO.setId(1L);
        when(accountsDAO.findById(1L)).thenReturn(Optional.of(toReturnAfterSaveFromDAO));

        var account = accountsService.getById(1L)
                .orElseThrow();

        assertEquals(1, account.getId().longValue());
        assertEquals(BigDecimal.TEN.compareTo(account.getBalance().getValue()), 0);
    }

    @Test
    void getByIdShouldReturnEmptyOptionalWhenAccountDoesNotExist() {
        var account = accountsService.getById(1L);

        assertTrue(account.isEmpty());
    }

    @Test
    void saveShouldSaveTheAccountUsingDAO() {
        when(accountsDAO.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            var accountEntity = (AccountEntity) invocation.getArgument(0);
            accountEntity.setId(1L);

            return accountEntity;
        });

        var savedAccountEntity = accountsService.save(new Account(null, Money.valueOf(10)));

        var accountEntityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountsDAO).save(accountEntityCaptor.capture());

        var entityRequestedToBeSaved = accountEntityCaptor.getValue();

        assertEquals(savedAccountEntity.getBalance().getValue().compareTo(entityRequestedToBeSaved.getBalance()), 0);
        assertEquals(savedAccountEntity.getId(), entityRequestedToBeSaved.getId());
    }
}
