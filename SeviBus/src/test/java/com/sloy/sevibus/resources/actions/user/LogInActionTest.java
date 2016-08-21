package com.sloy.sevibus.resources.actions.user;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sloy.sevibus.resources.CrashReportingTool;
import com.sloy.sevibus.resources.datasource.user.UserDataSource;
import com.sloy.sevibus.resources.services.LoginService;
import com.sloy.sevibus.ui.SevibusUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class LogInActionTest {

    @Mock
    UserDataSource userDataSource;
    @Mock
    FirebaseDatabase firebaseDatabase;
    @Mock
    CrashReportingTool crashReportingTool;
    @Mock
    AuthCredential credential;
    @Mock
    SevibusUser sevibusUser;
    @Mock
    LoginService loginService;
    @Mock
    DatabaseReference firebaseReference;

    private LogInAction action;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new LogInAction(userDataSource, loginService, firebaseDatabase, crashReportingTool);
        when(userDataSource.setCurrentUser(any(SevibusUser.class))).thenReturn(just(new SevibusUser()));
        when(firebaseDatabase.getReference()).thenReturn(firebaseReference);
        when(firebaseDatabase.getReference(anyString())).thenReturn(firebaseReference);
        when(firebaseReference.child(anyString())).thenReturn(firebaseReference);
    }

    @Test
    public void set_current_user_when_service_return_data() throws Exception {
        givenLoginServiceAuthRespondsOK();

        action.logIn(credential)
          .toBlocking().subscribe();

        //TODO verify the path where the user was saved. Right now we have one single mock for all child nodes
        verify(userDataSource).setCurrentUser(any(SevibusUser.class));
    }

    @Test
    public void set_current_user_on_firebase_when_firebase_return_data() throws Exception {
        givenLoginServiceAuthRespondsOK();

        action.logIn(credential)
          .toBlocking().subscribe();

        verify(firebaseReference).setValue(any(SevibusUser.class));
    }

    @Test
    public void associate_user_with_crash_reporting_tool() throws Exception {
        givenLoginServiceAuthRespondsOK();

        action.logIn(credential)
          .toBlocking().subscribe();

        verify(crashReportingTool).associateUser(any(SevibusUser.class));
    }

    private void givenLoginServiceAuthRespondsOK() {
        when(loginService.logUserIn(credential)).thenReturn(just(sevibusUser));
    }
}