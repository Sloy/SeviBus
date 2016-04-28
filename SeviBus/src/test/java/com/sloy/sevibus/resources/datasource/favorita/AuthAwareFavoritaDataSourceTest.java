package com.sloy.sevibus.resources.datasource.favorita;

import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.*;

public class AuthAwareFavoritaDataSourceTest {

    @Mock
    UserDataSource userDataSource;

    private AuthAwareFavoritaDataSource dataSource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        dataSource = new AuthAwareFavoritaDataSource(null, userDataSource);
        when(userDataSource.getCurrentUser()).thenReturn(empty());
    }

    @Test
    public void return_error_when_unknown_exception() throws Exception {
        TestException anyError = new TestException();
        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();

        dataSource.errorWhenLoggedIn(anyError)
          .subscribe(testSubscriber);

        testSubscriber.assertError(TestException.class);
    }

    @Test
    public void return_empty_when_authexception_and_current_user_empty() throws Exception {
        when(userDataSource.getCurrentUser()).thenReturn(empty());
        AuthException authError = new AuthException();
        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();

        dataSource.errorWhenLoggedIn(authError)
          .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();
    }

    @Test
    public void return_auth_error_when_authexception_and_current_user_exist() throws Exception {
        when(userDataSource.getCurrentUser()).thenReturn(just(new SevibusUser()));
        AuthException authError = new AuthException();
        TestSubscriber<Object> testSubscriber = new TestSubscriber<>();

        dataSource.errorWhenLoggedIn(authError)
          .subscribe(testSubscriber);

        testSubscriber.assertError(AuthException.class);
    }

    private class TestException extends Exception {
    }

}