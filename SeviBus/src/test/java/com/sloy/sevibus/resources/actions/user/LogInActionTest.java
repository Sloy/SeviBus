package com.sloy.sevibus.resources.actions.user;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.sloy.sevibus.resources.CrashReportingTool;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class LogInActionTest {

    @Mock
    UserDataSource userDataSource;
    @Mock
    Firebase firebase;
    @Mock
    CrashReportingTool crashReportingTool;
    @Mock
    AuthData authData;

    private LogInAction action;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new LogInAction(userDataSource, firebase, crashReportingTool);
        when(userDataSource.setCurrentUser(any(SevibusUser.class))).thenReturn(just(new SevibusUser()));
        when(firebase.child(anyString())).thenReturn(firebase);
    }

    @Test
    public void set_current_user_when_firebase_return_data() throws Exception {
        givenFirebaseAuthRespondsOK();

        action.logIn("token")
          .toBlocking().subscribe();

        //TODO verify the path where the user was saved. Right now we have one single mock for all child nodes
        verify(userDataSource).setCurrentUser(any(SevibusUser.class));
    }

    @Test
    public void set_current_user_on_firebase_when_firebase_return_data() throws Exception {
        givenFirebaseAuthRespondsOK();

        action.logIn("token")
          .toBlocking().subscribe();

        verify(firebase).setValue(any(SevibusUser.class));
    }

    @Test
    public void associate_user_with_crash_reporting_tool() throws Exception {
        givenFirebaseAuthRespondsOK();

        action.logIn("token")
          .toBlocking().subscribe();

        verify(crashReportingTool).associateUser(any(SevibusUser.class));
    }

    private void givenFirebaseAuthRespondsOK() {
        doAnswer(i -> {
            ((Firebase.AuthResultHandler) i.getArguments()[2]).onAuthenticated(authData);
            return null;
        }).when(firebase).authWithOAuthToken(eq("google"), anyString(), any(Firebase.AuthResultHandler.class));
        when(firebase.getAuth()).thenReturn(authData);
    }
}