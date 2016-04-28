package com.sloy.sevibus.resources.actions.user;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.ui.SevibusUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LogInActionTest {

    @Mock
    UserDataSource userDataSource;
    @Mock
    Firebase firebase;

    private LogInAction action;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new LogInAction(userDataSource, firebase);
    }

    @Test
    public void set_current_user_when_firebase_return_data() throws Exception {
        givenFirebaseAuthResponds(mock(AuthData.class));

        action.logIn("token")
          .toBlocking().subscribe();

        verify(userDataSource).setCurrentUser(any(SevibusUser.class));
    }

    private void givenFirebaseAuthResponds(AuthData authData) {
        doAnswer(i -> {
            ((Firebase.AuthResultHandler) i.getArguments()[2]).onAuthenticated(authData);
            return null;
        }).when(firebase).authWithOAuthToken(eq("google"), anyString(), any(Firebase.AuthResultHandler.class));
    }
}