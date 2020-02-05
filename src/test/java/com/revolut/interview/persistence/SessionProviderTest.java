package com.revolut.interview.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionProviderTest {

    @Mock
    private SessionFactory sessionFactory;

    private SessionProvider sessionProvider;

    @BeforeEach
    void setUp() {
        this.sessionProvider = new SessionProvider(sessionFactory);
    }

    @Test
    void getShouldOpenSessionFromSessionFactory() {
        var expected = mock(Session.class);
        when(sessionFactory.getCurrentSession()).thenReturn(expected);

        var actual = sessionProvider.get();

        assertEquals(expected, actual);
    }
}
